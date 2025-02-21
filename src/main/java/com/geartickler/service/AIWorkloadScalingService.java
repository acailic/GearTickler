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

import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class AIWorkloadScalingService {
  private static final Logger log = LoggerFactory.getLogger(AIWorkloadScalingService.class);
  private final KubernetesClient kubernetesClient;
  private final Map<String, ScalingHistory> scalingHistory;

  private static final double GPU_UTILIZATION_THRESHOLD_HIGH = 0.85;
  private static final double GPU_UTILIZATION_THRESHOLD_LOW = 0.3;
  private static final double LATENCY_THRESHOLD_MS = 100.0;
  private static final double QUEUE_LENGTH_THRESHOLD = 100;
  private static final int SCALING_COOLDOWN_SECONDS = 300;

  @Inject
  public AIWorkloadScalingService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
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

    return System.currentTimeMillis() - history.lastScalingTime > SCALING_COOLDOWN_SECONDS * 1000;
  }

  private ScalingDecision calculateScalingDecision(MetricsStatus metrics, ScalingStatus currentScaling) {
    ScalingDecision decision = new ScalingDecision();

    // Check GPU utilization
    if (metrics.getGpuUtilization() > GPU_UTILIZATION_THRESHOLD_HIGH) {
      decision.setScaleUp(true);
      decision.addReason("High GPU utilization: " + metrics.getGpuUtilization());
    } else if (metrics.getGpuUtilization() < GPU_UTILIZATION_THRESHOLD_LOW) {
      decision.setScaleDown(true);
      decision.addReason("Low GPU utilization: " + metrics.getGpuUtilization());
    }

    // Check inference latency
    if (metrics.getAverageInferenceLatency() > LATENCY_THRESHOLD_MS) {
      decision.setScaleUp(true);
      decision.addReason("High inference latency: " + metrics.getAverageInferenceLatency());
    }

    // Check queue length
    if (metrics.getQueueLength() > QUEUE_LENGTH_THRESHOLD) {
      decision.setScaleUp(true);
      decision.addReason("Long queue length: " + metrics.getQueueLength());
    }

    // Check GPU memory
    if (metrics.getGpuMemoryUtilization() > 0.9) {
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
    double baseAllocation = 1.0;
    double utilizationFactor = metrics.getGpuUtilization() / 0.7; // Target 70% utilization
    double memoryFactor = metrics.getGpuMemoryUtilization() / 0.8; // Target 80% memory utilization
    double queueFactor = Math.max(1.0, metrics.getQueueLength() / QUEUE_LENGTH_THRESHOLD);

    return baseAllocation * Math.max(utilizationFactor, Math.max(memoryFactor, queueFactor));
  }

  private double calculateOptimalMemoryAllocation(MetricsStatus metrics) {
    // Base memory per model instance (in GB)
    double baseMemory = 4.0;
    double utilizationFactor = metrics.getMemoryUtilization() / 0.8; // Target 80% utilization
    return baseMemory * utilizationFactor;
  }

  private double calculateOptimalCpuAllocation(MetricsStatus metrics) {
    // Base CPU cores per model instance
    double baseCpu = 2.0;
    double utilizationFactor = metrics.getCpuUtilization() / 0.8; // Target 80% utilization
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

  private static class ScalingHistory {
    long lastScalingTime;
    ScalingDecision decision;
  }

  private static class ScalingDecision {
    private boolean scaleUp;
    private boolean scaleDown;
    private List<String> reasons = new ArrayList<>();
    private Map<String, ResourceAllocation> resourceAllocations;

    public boolean isScaleUp() {
      return scaleUp;
    }

    public void setScaleUp(boolean value) {
      this.scaleUp = value;
    }

    public boolean isScaleDown() {
      return scaleDown;
    }

    public void setScaleDown(boolean value) {
      this.scaleDown = value;
    }

    public List<String> getReasons() {
      return reasons;
    }

    public void addReason(String reason) {
      this.reasons.add(reason);
    }

    public Map<String, ResourceAllocation> getResourceAllocations() {
      return resourceAllocations;
    }

    public void setResourceAllocations(Map<String, ResourceAllocation> value) {
      this.resourceAllocations = value;
    }

    public boolean shouldScale() {
      return scaleUp || scaleDown;
    }
  }
}
