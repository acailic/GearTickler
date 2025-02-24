package com.geartickler.service;

import com.geartickler.model.AIWorkload;
import com.geartickler.model.AIWorkloadStatus.MetricsStatus;
import com.geartickler.model.AIWorkloadStatus.ScalingStatus;
import com.geartickler.model.AIWorkloadStatus.ResourceAllocation;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import com.geartickler.config.MetricsConfig;

@ApplicationScoped
public class AIWorkloadScalingService {
  private static final Logger log = LoggerFactory.getLogger(AIWorkloadScalingService.class);
  private final KubernetesClient kubernetesClient;
  private final Map<String, ScalingHistory> scalingHistory;
  private final MetricsConfig config;

  @Inject
  public AIWorkloadScalingService(KubernetesClient kubernetesClient, MetricsConfig config) {
    this.kubernetesClient = kubernetesClient;
    this.config = config;
    this.scalingHistory = new ConcurrentHashMap<>();
  }

  public void evaluateScaling(AIWorkload workload) {
    String key = workload.getMetadata().getNamespace() + "/" + workload.getMetadata().getName();
    MetricsStatus metrics = workload.getStatus().getMetrics();
    ScalingStatus scaling = workload.getStatus().getScaling();

    if (!canScale(key)) {
      log.debug("Scaling cooldown active for {}", key);
      return;
    }

    ScalingDecision decision = calculateScalingDecision(metrics, scaling);
    if (decision.shouldScale()) {
      applyScaling(workload, decision);
      updateScalingHistory(key, decision);
    }
  }

  private boolean canScale(String key) {
    ScalingHistory history = scalingHistory.get(key);
    if (history == null)
      return true;

    return System.currentTimeMillis() - history.lastScalingTime > config.scaling().scalingCooldownSeconds() * 1000;
  }

  private ScalingDecision calculateScalingDecision(MetricsStatus metrics, ScalingStatus currentScaling) {
    ScalingDecision decision = new ScalingDecision();

    // Check GPU utilization
    if (metrics.getGpuUtilization() > config.scaling().gpuUtilizationThresholdHigh()) {
      decision.setScaleUp(true);
      decision.addReason("High GPU utilization: " + metrics.getGpuUtilization());
    } else if (metrics.getGpuUtilization() < config.scaling().gpuUtilizationThresholdLow()) {
      decision.setScaleDown(true);
      decision.addReason("Low GPU utilization: " + metrics.getGpuUtilization());
    }

    // Check inference latency
    if (metrics.getAverageInferenceLatency() > config.scaling().latencyThresholdMs()) {
      decision.setScaleUp(true);
      decision.addReason("High inference latency: " + metrics.getAverageInferenceLatency());
    }

    // Check queue length
    if (metrics.getQueueLength() > config.scaling().queueLengthThreshold()) {
      decision.setScaleUp(true);
      decision.addReason("Long queue length: " + metrics.getQueueLength());
    }

    // Check GPU memory
    if (metrics.getGpuMemoryUtilization() > config.scaling().gpuMemoryThresholdHigh()) {
      decision.setScaleUp(true);
      decision.addReason("High GPU memory utilization: " + metrics.getGpuMemoryUtilization());
    }

    // Determine optimal resource allocation
    Map<String, ResourceAllocation> newAllocations = calculateOptimalResources(metrics, currentScaling);
    decision.setResourceAllocations(newAllocations);

    return decision;
  }

  private Map<String, ResourceAllocation> calculateOptimalResources(MetricsStatus metrics,
      ScalingStatus currentScaling) {
    Map<String, ResourceAllocation> allocations = new HashMap<>();

    // Calculate GPU requirements
    ResourceAllocation gpuAllocation = new ResourceAllocation();
    gpuAllocation.setResourceType("GPU");
    gpuAllocation.setRequested(calculateOptimalGpuAllocation(metrics));
    allocations.put("gpu", gpuAllocation);

    // Calculate memory requirements
    ResourceAllocation memoryAllocation = new ResourceAllocation();
    memoryAllocation.setResourceType("Memory");
    memoryAllocation.setRequested(calculateOptimalMemoryAllocation(metrics));
    allocations.put("memory", memoryAllocation);

    // Calculate CPU requirements
    ResourceAllocation cpuAllocation = new ResourceAllocation();
    cpuAllocation.setResourceType("CPU");
    cpuAllocation.setRequested(calculateOptimalCpuAllocation(metrics));
    allocations.put("cpu", cpuAllocation);

    return allocations;
  }

  private double calculateOptimalGpuAllocation(MetricsStatus metrics) {
    double baseAllocation = config.resources().baseGpuUnits();
    double utilizationFactor = metrics.getGpuUtilization() / config.scaling().targetGpuUtilization();
    double memoryFactor = metrics.getGpuMemoryUtilization() / config.scaling().targetMemoryUtilization();
    double queueFactor = Math.max(1.0, metrics.getQueueLength() / config.scaling().queueLengthThreshold());

    return baseAllocation * Math.max(utilizationFactor, Math.max(memoryFactor, queueFactor));
  }

  private double calculateOptimalMemoryAllocation(MetricsStatus metrics) {
    double baseMemory = config.resources().baseMemoryGb();
    double utilizationFactor = metrics.getMemoryUtilization() / config.scaling().targetMemoryUtilization();
    return baseMemory * utilizationFactor;
  }

  private double calculateOptimalCpuAllocation(MetricsStatus metrics) {
    double baseCpu = config.resources().baseCpuCores();
    double utilizationFactor = metrics.getCpuUtilization() / config.scaling().targetCpuUtilization();
    return baseCpu * utilizationFactor;
  }

  private void applyScaling(AIWorkload workload, ScalingDecision decision) {
    ScalingStatus scaling = workload.getStatus().getScaling();

    // Update replicas
    int newReplicas = calculateNewReplicaCount(scaling.getCurrentReplicas(), decision);
    scaling.setDesiredReplicas(newReplicas);
    scaling.setScalingReason(String.join(", ", decision.getReasons()));

    // Update resource allocations
    scaling.setResourceAllocations(decision.getResourceAllocations());

    // Apply changes to Kubernetes
    updateKubernetesResources(workload, decision);

    // Update status
    kubernetesClient.resource(workload).updateStatus();
  }

  private int calculateNewReplicaCount(int currentReplicas, ScalingDecision decision) {
    if (decision.isScaleUp()) {
      return currentReplicas + 1;
    } else if (decision.isScaleDown()) {
      return Math.max(1, currentReplicas - 1);
    }
    return currentReplicas;
  }

  private void updateKubernetesResources(AIWorkload workload, ScalingDecision decision) {
    String namespace = workload.getMetadata().getNamespace();
    String name = workload.getMetadata().getName();

    // Update deployment resources
    kubernetesClient.apps().deployments()
        .inNamespace(namespace)
        .withName(name)
        .edit(deployment -> {
          deployment.getSpec().setReplicas(workload.getStatus().getScaling().getDesiredReplicas());
          updateContainerResources(deployment, decision.getResourceAllocations());
          return deployment;
        });
  }

  private void updateScalingHistory(String key, ScalingDecision decision) {
    ScalingHistory history = new ScalingHistory();
    history.lastScalingTime = System.currentTimeMillis();
    history.decision = decision;
    scalingHistory.put(key, history);
  }

  @Getter
  @Setter
  private static class ScalingHistory {
    private long lastScalingTime;
    private ScalingDecision decision;
  }

  @Data
  private static class ScalingDecision {
    private boolean scaleUp;
    private boolean scaleDown;
    private List<String> reasons = new ArrayList<>();
    private Map<String, ResourceAllocation> resourceAllocations;

    public boolean shouldScale() {
      return scaleUp || scaleDown;
    }
  }
}
