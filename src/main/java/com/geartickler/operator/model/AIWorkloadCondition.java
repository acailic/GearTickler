package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

@Deprecated
public class AIWorkloadCondition {

  @JsonProperty("type")
  private String type;

  @JsonProperty("status")
  private String status;

  @JsonProperty("lastTransitionTime")
  private String lastTransitionTime;

  @JsonProperty("reason")
  private String reason;

  @JsonProperty("message")
  private String message;

}
