package com.geartickler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.geartickler.model.AutoscalingPolicy;
import com.geartickler.model.ModelVersion;

@Data
@NoArgsConstructor
public class AIWorkloadStatus {

  private String phase;
  private String message;
  private List<String> conditions = new ArrayList<>();
  private MetricsStatus metrics = new MetricsStatus();
  private ScalingStatus scaling = new ScalingStatus();
  private ModelStatus model = new ModelStatus();

  @Data
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
  }

  @Data
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
  }

  @Data
  public static class ScalingStatus {
    private int currentReplicas;
    private int desiredReplicas;
    private String scalingReason;
    private Map<String, ResourceAllocation> resourceAllocations;
    private AutoscalingPolicy currentPolicy;
  }

  @Data
  public static class ResourceAllocation {
    private String resourceType;
    private double requested;
    private double allocated;
    private double utilized;
    private String deviceId;
    private Map<String, String> deviceProperties;
  }

  @Data
  public static class ModelStatus {
    private String version;
    private String format;
    private String state;
    private String healthStatus;
    private List<String> supportedDevices;
    private Map<String, String> modelProperties;
    private List<ModelVersion> deployedVersions;
  }
}
