package com.geartickler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AIWorkloadStatus {

  private String phase;
  private String message;
  private List<String> conditions;
  private MetricsStatus metrics;
  private ScalingStatus scaling;
  private ModelStatus model;

  public AIWorkloadStatus() {
    this.conditions = new ArrayList<>();
    this.metrics = new MetricsStatus();
    this.scaling = new ScalingStatus();
    this.model = new ModelStatus();
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

  public ScalingStatus getScaling() {
    return scaling;
  }

  public void setScaling(ScalingStatus scaling) {
    this.scaling = scaling;
  }

  public ModelStatus getModel() {
    return model;
  }

  public void setModel(ModelStatus model) {
    this.model = model;
  }

  public static class MetricsStatus {
    private double cpuUtilization;
    private double gpuUtilization;
    private double averageInferenceLatency;
    private double throughput;
    private double memoryUtilization;
    private double gpuMemoryUtilization;
    private double batchSize;
    private double queueLength;
    private ModelMetrics modelMetrics;

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

    public double getMemoryUtilization() {
      return memoryUtilization;
    }

    public void setMemoryUtilization(double memoryUtilization) {
      this.memoryUtilization = memoryUtilization;
    }

    public double getGpuMemoryUtilization() {
      return gpuMemoryUtilization;
    }

    public void setGpuMemoryUtilization(double gpuMemoryUtilization) {
      this.gpuMemoryUtilization = gpuMemoryUtilization;
    }

    public double getBatchSize() {
      return batchSize;
    }

    public void setBatchSize(double batchSize) {
      this.batchSize = batchSize;
    }

    public double getQueueLength() {
      return queueLength;
    }

    public void setQueueLength(double queueLength) {
      this.queueLength = queueLength;
    }

    public ModelMetrics getModelMetrics() {
      return modelMetrics;
    }

    public void setModelMetrics(ModelMetrics modelMetrics) {
      this.modelMetrics = modelMetrics;
    }
  }

  public static class ModelMetrics {
    private double accuracy;
    private double f1Score;
    private double precision;
    private double recall;
    private double auc;
    private double mae;
    private double mse;
    private long totalPredictions;
    private double averagePredictionTime;
    private Map<String, Double> customMetrics;

    public double getAccuracy() {
      return accuracy;
    }

    public void setAccuracy(double accuracy) {
      this.accuracy = accuracy;
    }

    public double getF1Score() {
      return f1Score;
    }

    public void setF1Score(double f1Score) {
      this.f1Score = f1Score;
    }

    public double getPrecision() {
      return precision;
    }

    public void setPrecision(double precision) {
      this.precision = precision;
    }

    public double getRecall() {
      return recall;
    }

    public void setRecall(double recall) {
      this.recall = recall;
    }

    public double getAuc() {
      return auc;
    }

    public void setAuc(double auc) {
      this.auc = auc;
    }

    public double getMae() {
      return mae;
    }

    public void setMae(double mae) {
      this.mae = mae;
    }

    public double getMse() {
      return mse;
    }

    public void setMse(double mse) {
      this.mse = mse;
    }

    public long getTotalPredictions() {
      return totalPredictions;
    }

    public void setTotalPredictions(long totalPredictions) {
      this.totalPredictions = totalPredictions;
    }

    public double getAveragePredictionTime() {
      return averagePredictionTime;
    }

    public void setAveragePredictionTime(double averagePredictionTime) {
      this.averagePredictionTime = averagePredictionTime;
    }

    public Map<String, Double> getCustomMetrics() {
      return customMetrics;
    }

    public void setCustomMetrics(Map<String, Double> customMetrics) {
      this.customMetrics = customMetrics;
    }
  }

  public static class ScalingStatus {
    private int currentReplicas;
    private int desiredReplicas;
    private String scalingReason;
    private Map<String, ResourceAllocation> resourceAllocations;
    private AutoscalingPolicy currentPolicy;

    public int getCurrentReplicas() {
      return currentReplicas;
    }

    public void setCurrentReplicas(int currentReplicas) {
      this.currentReplicas = currentReplicas;
    }

    public int getDesiredReplicas() {
      return desiredReplicas;
    }

    public void setDesiredReplicas(int desiredReplicas) {
      this.desiredReplicas = desiredReplicas;
    }

    public String getScalingReason() {
      return scalingReason;
    }

    public void setScalingReason(String scalingReason) {
      this.scalingReason = scalingReason;
    }

    public Map<String, ResourceAllocation> getResourceAllocations() {
      return resourceAllocations;
    }

    public void setResourceAllocations(Map<String, ResourceAllocation> resourceAllocations) {
      this.resourceAllocations = resourceAllocations;
    }

    public AutoscalingPolicy getCurrentPolicy() {
      return currentPolicy;
    }

    public void setCurrentPolicy(AutoscalingPolicy currentPolicy) {
      this.currentPolicy = currentPolicy;
    }
  }

  public static class ResourceAllocation {
    private String resourceType;
    private double requested;
    private double allocated;
    private double utilized;
    private String deviceId;
    private Map<String, String> deviceProperties;

    public String getResourceType() {
      return resourceType;
    }

    public void setResourceType(String resourceType) {
      this.resourceType = resourceType;
    }

    public double getRequested() {
      return requested;
    }

    public void setRequested(double requested) {
      this.requested = requested;
    }

    public double getAllocated() {
      return allocated;
    }

    public void setAllocated(double allocated) {
      this.allocated = allocated;
    }

    public double getUtilized() {
      return utilized;
    }

    public void setUtilized(double utilized) {
      this.utilized = utilized;
    }

    public String getDeviceId() {
      return deviceId;
    }

    public void setDeviceId(String deviceId) {
      this.deviceId = deviceId;
    }

    public Map<String, String> getDeviceProperties() {
      return deviceProperties;
    }

    public void setDeviceProperties(Map<String, String> deviceProperties) {
      this.deviceProperties = deviceProperties;
    }
  }

  public static class ModelStatus {
    private String version;
    private String format;
    private String state;
    private String healthStatus;
    private List<String> supportedDevices;
    private Map<String, String> modelProperties;
    private List<ModelVersion> deployedVersions;

    public String getVersion() {
      return version;
    }

    public void setVersion(String version) {
      this.version = version;
    }

    public String getFormat() {
      return format;
    }

    public void setFormat(String format) {
      this.format = format;
    }

    public String getState() {
      return state;
    }

    public void setState(String state) {
      this.state = state;
    }

    public String getHealthStatus() {
      return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
      this.healthStatus = healthStatus;
    }

    public List<String> getSupportedDevices() {
      return supportedDevices;
    }

    public void setSupportedDevices(List<String> supportedDevices) {
      this.supportedDevices = supportedDevices;
    }

    public Map<String, String> getModelProperties() {
      return modelProperties;
    }

    public void setModelProperties(Map<String, String> modelProperties) {
      this.modelProperties = modelProperties;
    }

    public List<ModelVersion> getDeployedVersions() {
      return deployedVersions;
    }

    public void setDeployedVersions(List<ModelVersion> deployedVersions) {
      this.deployedVersions = deployedVersions;
    }
  }
}
