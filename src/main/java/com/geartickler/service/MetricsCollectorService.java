package com.geartickler.service;

import com.geartickler.config.MetricsConfig;
import com.geartickler.model.AIWorkload;
import com.geartickler.model.AIWorkloadStatus.MetricsStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.PreDestroy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class MetricsCollectorService {
  private static final Logger log = LoggerFactory.getLogger(MetricsCollectorService.class);
  private final KubernetesClient kubernetesClient;
  private final ScheduledExecutorService scheduler;
  private final GpuMetricsService gpuMetricsService;
  private final PrometheusMetricsService prometheusMetricsService;
  private final Map<String, Boolean> activeCollectors;
  private final MetricsConfig config;

  @Inject
  public MetricsCollectorService(
      KubernetesClient kubernetesClient,
      GpuMetricsService gpuMetricsService,
      PrometheusMetricsService prometheusMetricsService,
      MetricsConfig config) {
    this.kubernetesClient = kubernetesClient;
    this.gpuMetricsService = gpuMetricsService;
    this.prometheusMetricsService = prometheusMetricsService;
    this.config = config;
    this.scheduler = Executors.newScheduledThreadPool(config.scheduler().poolSize());
    this.activeCollectors = new ConcurrentHashMap<>();
  }

  public void startMetricsCollection(AIWorkload resource) {
    String name = resource.getMetadata().getName();
    String namespace = resource.getMetadata().getNamespace();
    String key = namespace + "/" + name;

    if (activeCollectors.putIfAbsent(key, true) != null) {
      log.debug("Metrics collection already active for {}", key);
      return;
    }

    scheduler.scheduleAtFixedRate(
        () -> collectMetrics(resource),
        0,
        config.collection().interval().toSeconds(),
        TimeUnit.SECONDS);
  }

  private void collectMetrics(AIWorkload resource) {
    String name = resource.getMetadata().getName();
    String namespace = resource.getMetadata().getNamespace();

    try {
      CompletableFuture<Double> cpuFuture = CompletableFuture.supplyAsync(() -> kubernetesClient.top().pods()
          .metrics(namespace)
          .stream()
          .filter(m -> m.getPodName().startsWith(name))
          .mapToDouble(m -> m.getCpuUsage().doubleValue())
          .average()
          .orElse(0.0));

      CompletableFuture<Double> gpuFuture = gpuMetricsService.fetchGpuUtilization();
      CompletableFuture<Double> latencyFuture = prometheusMetricsService.fetchMetric(
          namespace, name, "inference_latency_seconds");
      CompletableFuture<Double> throughputFuture = prometheusMetricsService.fetchMetric(
          namespace, name, "inference_requests_total");

      CompletableFuture.allOf(cpuFuture, gpuFuture, latencyFuture, throughputFuture)
          .thenAccept(v -> {
            MetricsStatus metrics = new MetricsStatus();
            metrics.setCpuUtilization(cpuFuture.join());
            metrics.setGpuUtilization(gpuFuture.join());
            metrics.setAverageInferenceLatency(latencyFuture.join());
            metrics.setThroughput(throughputFuture.join());
            updateWorkloadMetrics(resource, metrics);
          })
          .exceptionally(e -> {
            log.error("Error collecting metrics for {}/{}", namespace, name, e);
            return null;
          });

    } catch (Exception e) {
      log.error("Error initiating metrics collection for {}/{}", namespace, name, e);
    }
  }

  private void updateWorkloadMetrics(AIWorkload resource, MetricsStatus metrics) {
    resource.getStatus().setMetrics(metrics);
    kubernetesClient.resource(resource).updateStatus();
  }

  public void stopMetricsCollection(String namespace, String name) {
    String key = namespace + "/" + name;
    activeCollectors.remove(key);
  }

  @PreDestroy
  public void cleanup() {
    if (scheduler != null) {
      scheduler.shutdown();
      try {
        if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
          scheduler.shutdownNow();
        }
      } catch (InterruptedException e) {
        scheduler.shutdownNow();
        Thread.currentThread().interrupt();
      }
    }
  }
}
