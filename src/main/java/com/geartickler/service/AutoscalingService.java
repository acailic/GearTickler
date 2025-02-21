package com.geartickler.service;

import com.geartickler.model.AIWorkload;
import com.geartickler.model.AutoscalingSpec;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscaler;
import io.fabric8.kubernetes.api.model.autoscaling.v2.HorizontalPodAutoscalerBuilder;
import io.fabric8.kubernetes.api.model.autoscaling.v2.MetricSpecBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AutoscalingService {

  private static final Logger log = LoggerFactory.getLogger(AutoscalingService.class);
  private final KubernetesClient kubernetesClient;

  @Inject
  public AutoscalingService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public void createOrUpdateHPA(AIWorkload resource) {
    AutoscalingSpec spec = resource.getSpec().getAutoscaling();
    if (spec == null || !spec.isEnabled()) {
      log.info("Autoscaling not enabled for {}", resource.getMetadata().getName());
      return;
    }

    HorizontalPodAutoscaler hpa = new HorizontalPodAutoscalerBuilder()
        .withNewMetadata()
        .withName(resource.getMetadata().getName())
        .withNamespace(resource.getMetadata().getNamespace())
        .endMetadata()
        .withNewSpec()
        .withMinReplicas(spec.getMinReplicas())
        .withMaxReplicas(spec.getMaxReplicas())
        .withNewScaleTargetRef()
        .withApiVersion("apps/v1")
        .withKind("Deployment")
        .withName(resource.getMetadata().getName())
        .endScaleTargetRef()
        .withMetrics(
            new MetricSpecBuilder()
                .withType("Resource")
                .withNewResource()
                .withName("cpu")
                .withNewTarget()
                .withType("Utilization")
                .withAverageUtilization(spec.getTargetCPUUtilization())
                .endTarget()
                .endResource()
                .build(),
            new MetricSpecBuilder()
                .withType("Resource")
                .withNewResource()
                .withName("nvidia.com/gpu")
                .withNewTarget()
                .withType("Utilization")
                .withAverageUtilization(spec.getTargetGPUUtilization())
                .endTarget()
                .endResource()
                .build())
        .endSpec()
        .build();

    log.info("Creating/updating HPA for AIWorkload: {}", resource.getMetadata().getName());
    kubernetesClient.autoscaling().v2().horizontalPodAutoscalers()
        .inNamespace(resource.getMetadata().getNamespace())
        .createOrReplace(hpa);
  }

  public void deleteHPA(String namespace, String name) {
    log.info("Deleting HPA: {} in namespace: {}", name, namespace);
    kubernetesClient.autoscaling().v2().horizontalPodAutoscalers()
        .inNamespace(namespace)
        .withName(name)
        .delete();
  }
}
