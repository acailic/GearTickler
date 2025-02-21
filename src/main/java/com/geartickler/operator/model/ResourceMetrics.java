package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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

  // Getters and Setters
  public Double getCpuUtilization() {
    return cpuUtilization;
  }

  public void setCpuUtilization(Double cpuUtilization) {
    this.cpuUtilization = cpuUtilization;
  }

  public Double getGpuUtilization() {
    return gpuUtilization;
  }

  public void setGpuUtilization(Double gpuUtilization) {
    this.gpuUtilization = gpuUtilization;
  }

  public Double getMemoryUtilization() {
    return memoryUtilization;
  }

  public void setMemoryUtilization(Double memoryUtilization) {
    this.memoryUtilization = memoryUtilization;
  }

  public Double getInferenceLatency() {
    return inferenceLatency;
  }

  public void setInferenceLatency(Double inferenceLatency) {
    this.inferenceLatency = inferenceLatency;
  }

  public Double getRequestsPerSecond() {
    return requestsPerSecond;
  }

  public void setRequestsPerSecond(Double requestsPerSecond) {
    this.requestsPerSecond = requestsPerSecond;
  }
}
