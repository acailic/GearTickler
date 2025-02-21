package com.geartickler.operator.model;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;

@Version("v1alpha1")
@Group("geartickler.com")
public class AIWorkload extends CustomResource<AIWorkloadSpec, AIWorkloadStatus> implements Namespaced {

    @Override
    protected AIWorkloadSpec initSpec() {
        return new AIWorkloadSpec();
    }

    @Override
    protected AIWorkloadStatus initStatus() {
        return new AIWorkloadStatus();
    }
}
