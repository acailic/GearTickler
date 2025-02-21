package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.ResourceRequirements;

public class AIWorkloadSpec {

    @JsonProperty("modelName")
    private String modelName;

    @JsonProperty("modelVersion")
    private String modelVersion;

    @JsonProperty("framework")
    private String framework;

    @JsonProperty("replicas")
    private Integer replicas = 1;

    @JsonProperty("resources")
    private ResourceRequirements resources;

    @JsonProperty("inferenceEndpoint")
    private String inferenceEndpoint;

    @JsonProperty("autoscaling")
    private AutoscalingSpec autoscaling;

    // Getters and Setters
    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public String getFramework() {
        return framework;
    }

    public void setFramework(String framework) {
        this.framework = framework;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }

    public ResourceRequirements getResources() {
        return resources;
    }

    public void setResources(ResourceRequirements resources) {
        this.resources = resources;
    }

    public String getInferenceEndpoint() {
        return inferenceEndpoint;
    }

    public void setInferenceEndpoint(String inferenceEndpoint) {
        this.inferenceEndpoint = inferenceEndpoint;
    }

    public AutoscalingSpec getAutoscaling() {
        return autoscaling;
    }

    public void setAutoscaling(AutoscalingSpec autoscaling) {
        this.autoscaling = autoscaling;
    }
}
