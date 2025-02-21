package com.geartickler.model;

public class AutoscalingSpec {

    private boolean enabled;
    private int minReplicas;
    private int maxReplicas;
    private int targetCPUUtilization;
    private int targetGPUUtilization;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMinReplicas() {
        return minReplicas;
    }

    public void setMinReplicas(int minReplicas) {
        this.minReplicas = minReplicas;
    }

    public int getMaxReplicas() {
        return maxReplicas;
    }

    public void setMaxReplicas(int maxReplicas) {
        this.maxReplicas = maxReplicas;
    }

    public int getTargetCPUUtilization() {
        return targetCPUUtilization;
    }

    public void setTargetCPUUtilization(int targetCPUUtilization) {
        this.targetCPUUtilization = targetCPUUtilization;
    }

    public int getTargetGPUUtilization() {
        return targetGPUUtilization;
    }

    public void setTargetGPUUtilization(int targetGPUUtilization) {
        this.targetGPUUtilization = targetGPUUtilization;
    }
}
