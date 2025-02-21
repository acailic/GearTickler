package com.geartickler.service;

import com.geartickler.model.AIWorkload;
import com.geartickler.model.AIWorkloadStatus.MetricsStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class MetricsCollectorService {

  private static final Logger log = LoggerFactory.getLogger(MetricsCollectorService.class);
  private final KubernetesClient kubernetesClient;
  private final ScheduledExecutorService scheduler;

  @Inject
  public MetricsCollectorService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
    this.scheduler = Executors.newScheduledThreadPool(1);
  }

  public void startMetricsCollection(AIWorkload resource) {
    String name = resource.getMetadata().getName();
    String namespace = resource.getMetadata().getNamespace();

    scheduler.scheduleAtFixedRate(() -> {
      try {
        MetricsStatus metrics = collectMetrics(namespace, name);
        updateWorkloadMetrics(resource, metrics);
      } catch (Exception e) {
        log.error("Error collecting metrics for {}/{}", namespace, name, e);
      }
    }, 0, 30, TimeUnit.SECONDS);
  }

  private MetricsStatus collectMetrics(String namespace, String name) {
    MetricsStatus metrics = new MetricsStatus();

    // Collect CPU utilization
    double cpuUtilization = kubernetesClient.top().pods()
        .metrics(namespace)
        .stream()
        .filter(m -> m.getPodName().startsWith(name))
        .mapToDouble(m -> m.getCpuUsage().doubleValue())
        .average()
        .orElse(0.0);

    // Collect GPU utilization (using NVIDIA DCGM exporter metrics)
    double gpuUtilization = kubernetesClient.services()
        .inNamespace(namespace)
        .withName("dcgm-exporter")
        .get()
        .getStatus()
        .getLoadBalancer()
        .getIngress()
        .stream()
        .findFirst()
        .map(ingress -> fetchGPUMetrics(ingress.getIp()))
        .orElse(0.0);

    // Calculate inference latency and throughput from Prometheus metrics
    double latency = fetchPrometheusMetric(namespace, name, "inference_latency_seconds");
    double throughput = fetchPrometheusMetric(namespace, name, "inference_requests_total");

    metrics.setAverageInferenceLatency(latency);
    metrics.setThroughput(throughput);
    metrics.setCpuUtilization(cpuUtilization);
    metrics.setGpuUtilization(gpuUtilization);

    return metrics;
  }

  private double fetchGPUMetrics(String dcgmExporterIp) {
    // Implementation to fetch GPU metrics from NVIDIA DCGM exporter
    // This would typically involve making an HTTP request to the DCGM metrics
    // endpoint
    return 0.0; // Placeholder
  }

  private double fetchPrometheusMetric(String namespace, String name, String metricName) {
    // Implementation to fetch metrics from Prometheus
    // This would typically involve querying the Prometheus API
    return 0.0; // Placeholder
  }

  private void updateWorkloadMetrics(AIWorkload resource, MetricsStatus metrics) {
    resource.getStatus().setMetrics(metrics);
    kubernetesClient.resource(resource).updateStatus();
  }

  public void stopMetricsCollection(String name) {
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
