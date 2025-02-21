package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AutoscalingSpec {

  @JsonProperty("enabled")
  private Boolean enabled = false;

  @JsonProperty("minReplicas")
  private Integer minReplicas = 1;

  @JsonProperty("maxReplicas")
  private Integer maxReplicas = 10;

  @JsonProperty("targetCPUUtilization")
  private Integer targetCPUUtilization = 80;

  @JsonProperty("targetGPUUtilization")
  private Integer targetGPUUtilization = 80;

  @JsonProperty("scaleUpThreshold")
  private Integer scaleUpThreshold = 90;

  @JsonProperty("scaleDownThreshold")
  private Integer scaleDownThreshold = 50;

  // Getters and Setters
  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Integer getMinReplicas() {
    return minReplicas;
  }

  public void setMinReplicas(Integer minReplicas) {
    this.minReplicas = minReplicas;
  }

  public Integer getMaxReplicas() {
    return maxReplicas;
  }

  public void setMaxReplicas(Integer maxReplicas) {
    this.maxReplicas = maxReplicas;
  }

  public Integer getTargetCPUUtilization() {
    return targetCPUUtilization;
  }

  public void setTargetCPUUtilization(Integer targetCPUUtilization) {
    this.targetCPUUtilization = targetCPUUtilization;
  }

  public Integer getTargetGPUUtilization() {
    return targetGPUUtilization;
  }

  public void setTargetGPUUtilization(Integer targetGPUUtilization) {
    this.targetGPUUtilization = targetGPUUtilization;
  }

  public Integer getScaleUpThreshold() {
    return scaleUpThreshold;
  }

  public void setScaleUpThreshold(Integer scaleUpThreshold) {
    this.scaleUpThreshold = scaleUpThreshold;
  }

  public Integer getScaleDownThreshold() {
    return scaleDownThreshold;
  }

  public void setScaleDownThreshold(Integer scaleDownThreshold) {
    this.scaleDownThreshold = scaleDownThreshold;
  }
}
