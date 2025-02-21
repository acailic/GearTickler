package com.geartickler.model;

import java.util.ArrayList;
import java.util.List;

public class AIWorkloadStatus {

  private String phase;
  private String message;
  private List<String> conditions;
  private MetricsStatus metrics;

  public AIWorkloadStatus() {
    this.conditions = new ArrayList<>();
    this.metrics = new MetricsStatus();
  }

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public List<String> getConditions() {
    return conditions;
  }

  public void setConditions(List<String> conditions) {
    this.conditions = conditions;
  }

  public MetricsStatus getMetrics() {
    return metrics;
  }

  public void setMetrics(MetricsStatus metrics) {
    this.metrics = metrics;
  }

  public static class MetricsStatus {
    private double averageInferenceLatency;
    private double throughput;
    private double cpuUtilization;
    private double gpuUtilization;

    public double getAverageInferenceLatency() {
      return averageInferenceLatency;
    }

    public void setAverageInferenceLatency(double averageInferenceLatency) {
      this.averageInferenceLatency = averageInferenceLatency;
    }

    public double getThroughput() {
      return throughput;
    }

    public void setThroughput(double throughput) {
      this.throughput = throughput;
    }

    public double getCpuUtilization() {
      return cpuUtilization;
    }

    public void setCpuUtilization(double cpuUtilization) {
      this.cpuUtilization = cpuUtilization;
    }

    public double getGpuUtilization() {
      return gpuUtilization;
    }

    public void setGpuUtilization(double gpuUtilization) {
      this.gpuUtilization = gpuUtilization;
    }
  }
}
