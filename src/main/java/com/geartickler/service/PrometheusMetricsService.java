package com.geartickler.service;

import com.geartickler.config.MetricsConfig;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class PrometheusMetricsService {
  private static final Logger log = LoggerFactory.getLogger(PrometheusMetricsService.class);
  private final HttpClient httpClient;
  private final MetricsConfig config;
  private final ObjectMapper objectMapper;
  private final Map<String, String> queryCache;

  @Inject
  public PrometheusMetricsService(MetricsConfig config) {
    this.config = config;
    this.httpClient = HttpClient.newBuilder().build();
    this.objectMapper = new ObjectMapper();
    this.queryCache = new ConcurrentHashMap<>();
  }

  public CompletableFuture<Double> fetchMetric(String namespace, String name, String metricName) {
    String query = getOrBuildQuery(namespace, name, metricName);
    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(config.prometheus().url() + "?query=" + encodedQuery))
        .timeout(config.prometheus().timeout())
        .GET()
        .build();

    return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(response -> {
          if (response.statusCode() != 200) {
            log.error("Failed to fetch Prometheus metric {}. Status: {}",
                metricName, response.statusCode());
            return 0.0;
          }
          return parsePrometheusResponse(response.body(), metricName);
        })
        .exceptionally(e -> {
          log.error("Error fetching Prometheus metric: {}", metricName, e);
          return 0.0;
        });
  }

  private String getOrBuildQuery(String namespace, String name, String metricName) {
    String cacheKey = String.format("%s:%s:%s", namespace, name, metricName);
    return queryCache.computeIfAbsent(cacheKey, k -> buildPrometheusQuery(namespace, name, metricName));
  }

  private String buildPrometheusQuery(String namespace, String name, String metricName) {
    return switch (metricName) {
      case "inference_latency_seconds" ->
        String.format("avg(rate(%s{namespace=\"%s\",app=\"%s\"}[5m]))",
            metricName, namespace, name);
      case "inference_requests_total" ->
        String.format("sum(rate(%s{namespace=\"%s\",app=\"%s\"}[5m]))",
            metricName, namespace, name);
      default ->
        String.format("%s{namespace=\"%s\",app=\"%s\"}",
            metricName, namespace, name);
    };
  }

  private double parsePrometheusResponse(String responseBody, String metricName) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode result = root.path("data").path("result");

      if (result.isEmpty()) {
        log.debug("No data found for metric: {}", metricName);
        return 0.0;
      }

      JsonNode value = result.get(0).path("value");
      if (value.size() >= 2) {
        String metricValue = value.get(1).asText("0");
        return Double.parseDouble(metricValue);
      }

      return 0.0;
    } catch (Exception e) {
      log.warn("Error parsing Prometheus response for metric: {}", metricName, e);
      return 0.0;
    }
  }
}
