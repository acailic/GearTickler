package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
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

}
