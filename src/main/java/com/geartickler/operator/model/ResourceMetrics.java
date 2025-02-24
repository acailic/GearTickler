package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResourceMetrics {

  @JsonProperty("cpuUtilization")
  private Double cpuUtilization;

  @JsonProperty("gpuUtilization")
  private Double gpuUtilization;

  @JsonProperty("memoryUtilization")
  private Double memoryUtilization;

  @JsonProperty("inferenceLatency")
  private Double inferenceLatency;

  @JsonProperty("requestsPerSecond")
  private Double requestsPerSecond;

}
