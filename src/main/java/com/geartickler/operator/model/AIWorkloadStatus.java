package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

public class AIWorkloadStatus {

  @JsonProperty("phase")
  private String phase;

  @JsonProperty("availableReplicas")
  private Integer availableReplicas = 0;

  @JsonProperty("readyReplicas")
  private Integer readyReplicas = 0;

  @JsonProperty("conditions")
  private List<AIWorkloadCondition> conditions = new ArrayList<>();

  @JsonProperty("lastScalingTime")
  private String lastScalingTime;

  @JsonProperty("currentMetrics")
  private ResourceMetrics currentMetrics;

  // Getters and Setters
  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public Integer getAvailableReplicas() {
    return availableReplicas;
  }

  public void setAvailableReplicas(Integer availableReplicas) {
    this.availableReplicas = availableReplicas;
  }

  public Integer getReadyReplicas() {
    return readyReplicas;
  }

  public void setReadyReplicas(Integer readyReplicas) {
    this.readyReplicas = readyReplicas;
  }

  public List<AIWorkloadCondition> getConditions() {
    return conditions;
  }

  public void setConditions(List<AIWorkloadCondition> conditions) {
    this.conditions = conditions;
  }

  public String getLastScalingTime() {
    return lastScalingTime;
  }

  public void setLastScalingTime(String lastScalingTime) {
    this.lastScalingTime = lastScalingTime;
  }

  public ResourceMetrics getCurrentMetrics() {
    return currentMetrics;
  }

  public void setCurrentMetrics(ResourceMetrics currentMetrics) {
    this.currentMetrics = currentMetrics;
  }
}
