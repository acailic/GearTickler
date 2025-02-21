package com.geartickler.operator.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.fabric8.kubernetes.api.model.ResourceRequirements;

@Data
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

}
