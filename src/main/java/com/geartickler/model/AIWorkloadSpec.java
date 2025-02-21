package com.geartickler.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.ResourceRequirements;

public class AIWorkloadSpec {

    private String modelName;
    private String modelVersion;
    private String framework;
    private int replicas;
    private ResourceRequirements resources;
    private String inferenceEndpoint;
    private AutoscalingSpec autoscaling;

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

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
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
