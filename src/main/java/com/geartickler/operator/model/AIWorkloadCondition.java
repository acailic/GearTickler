package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;

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

  // Getters and Setters
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getLastTransitionTime() {
    return lastTransitionTime;
  }

  public void setLastTransitionTime(String lastTransitionTime) {
    this.lastTransitionTime = lastTransitionTime;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
