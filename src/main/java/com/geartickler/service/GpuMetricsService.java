package com.geartickler.service;

import com.geartickler.config.MetricsConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;

@ApplicationScoped
public class GpuMetricsService {
  private static final Logger log = LoggerFactory.getLogger(GpuMetricsService.class);
  private final HttpClient httpClient;
  private final MetricsConfig config;
  private static final Pattern GPU_UTIL_PATTERN = Pattern.compile("DCGM_FI_DEV_GPU_UTIL{.*} (\\d+)");

  @Inject
  public GpuMetricsService(MetricsConfig config) {
    this.config = config;
    this.httpClient = HttpClient.newBuilder().build();
  }

  public CompletableFuture<Double> fetchGpuUtilization() {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(config.dcgm().url() + "/metrics"))
        .GET()
        .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(response -> {
          if (response.statusCode() != 200) {
            log.error("Failed to fetch GPU metrics. HTTP Status: {}", response.statusCode());
            return 0.0;
          }
          return parseGpuMetrics(response.body());
        })
        .exceptionally(e -> {
          log.error("Error fetching GPU metrics from DCGM exporter", e);
          return 0.0;
        });
  }

  private double parseGpuMetrics(String metricsText) {
    double totalUtilization = 0.0;
    int gpuCount = 0;

    Matcher matcher = GPU_UTIL_PATTERN.matcher(metricsText);
    while (matcher.find()) {
      try {
        double utilization = Double.parseDouble(matcher.group(1));
        totalUtilization += utilization;
        gpuCount++;
      } catch (NumberFormatException e) {
        log.warn("Invalid GPU utilization value in metrics", e);
      }
    }

    if (gpuCount == 0) {
      log.warn("No GPU utilization metrics found");
      return 0.0;
    }

    double averageUtilization = totalUtilization / gpuCount;
    log.debug("Average GPU Utilization: {}%", averageUtilization);
    return averageUtilization;
  }
}
