package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
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

}
