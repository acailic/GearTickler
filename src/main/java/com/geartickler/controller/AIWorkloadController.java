package com.geartickler.controller;

import com.geartickler.model.AIWorkload;
import com.geartickler.model.AIWorkloadSpec;
import com.geartickler.model.AIWorkloadStatus;
import com.geartickler.service.AIWorkloadService;
import com.geartickler.service.AutoscalingService;
import com.geartickler.service.MetricsCollectorService;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.reconciler.*;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerConfiguration
public class AIWorkloadController implements Reconciler<AIWorkload> {

  private static final Logger log = LoggerFactory.getLogger(AIWorkloadController.class);
  private final KubernetesClient kubernetesClient;
  private final AIWorkloadService aiWorkloadService;
  private final AutoscalingService autoscalingService;
  private final MetricsCollectorService metricsCollectorService;

  @Inject
  public AIWorkloadController(
      KubernetesClient kubernetesClient,
      AIWorkloadService aiWorkloadService,
      AutoscalingService autoscalingService,
      MetricsCollectorService metricsCollectorService) {
    this.kubernetesClient = kubernetesClient;
    this.aiWorkloadService = aiWorkloadService;
    this.autoscalingService = autoscalingService;
    this.metricsCollectorService = metricsCollectorService;
  }

  @Override
  public UpdateControl<AIWorkload> reconcile(AIWorkload resource, Context<AIWorkload> context) {
    log.info("Reconciling AIWorkload: {}", resource.getMetadata().getName());

    try {
      // Create or update the deployment
      Deployment deployment = createDeployment(resource);
      kubernetesClient.apps().deployments()
          .inNamespace(resource.getMetadata().getNamespace())
          .createOrReplace(deployment);

      // Create or update the service
      aiWorkloadService.createOrUpdateService(resource);

      // Configure autoscaling if enabled
      autoscalingService.createOrUpdateHPA(resource);

      // Start metrics collection
      metricsCollectorService.startMetricsCollection(resource);

      // Update status
      updateStatus(resource, "Running", "Deployment, Service, and HPA reconciled successfully");

      return UpdateControl.updateStatus(resource);
    } catch (Exception e) {
      log.error("Error reconciling AIWorkload", e);
      updateStatus(resource, "Failed", e.getMessage());
      return UpdateControl.updateStatus(resource);
    }
  }

  @Override
  public DeleteControl cleanup(AIWorkload resource, Context<AIWorkload> context) {
    String namespace = resource.getMetadata().getNamespace();
    String name = resource.getMetadata().getName();

    try {
      // Stop metrics collection
      metricsCollectorService.stopMetricsCollection(name);

      // Delete HPA if exists
      autoscalingService.deleteHPA(namespace, name);

      // Delete service
      aiWorkloadService.deleteService(namespace, name);

      return DeleteControl.defaultDelete();
    } catch (Exception e) {
      log.error("Error cleaning up AIWorkload resources", e);
      return DeleteControl.defaultDelete();
    }
  }

  private Deployment createDeployment(AIWorkload resource) {
    AIWorkloadSpec spec = resource.getSpec();

    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(resource.getMetadata().getName())
        .withNamespace(resource.getMetadata().getNamespace())
        .addToLabels("app", resource.getMetadata().getName())
        .addToLabels("framework", spec.getFramework())
        .endMetadata()
        .withNewSpec()
        .withReplicas(spec.getReplicas())
        .withNewSelector()
        .addToMatchLabels("app", resource.getMetadata().getName())
        .endSelector()
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", resource.getMetadata().getName())
        .addToAnnotations("prometheus.io/scrape", "true")
        .addToAnnotations("prometheus.io/port", "8080")
        .addToAnnotations("prometheus.io/path", "/metrics")
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName("model-server")
        .withImage(getModelImage(spec))
        .withResources(spec.getResources())
        .addNewPort()
        .withContainerPort(8080)
        .withName("http")
        .endPort()
        .addNewEnv()
        .withName("MODEL_NAME")
        .withValue(spec.getModelName())
        .endEnv()
        .addNewEnv()
        .withName("MODEL_VERSION")
        .withValue(spec.getModelVersion())
        .endEnv()
        .addNewEnv()
        .withName("INFERENCE_ENDPOINT")
        .withValue(spec.getInferenceEndpoint())
        .endEnv()
        .withNewReadinessProbe()
        .withNewHttpGet()
        .withPath("/health")
        .withPort(8080)
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();
  }

  private String getModelImage(AIWorkloadSpec spec) {
    return String.format("geartickler/%s:%s",
        spec.getModelName().toLowerCase(),
        spec.getModelVersion());
  }

  private void updateStatus(AIWorkload resource, String phase, String message) {
    AIWorkloadStatus status = resource.getStatus();
    status.setPhase(phase);
    status.setMessage(message);
    status.getConditions().add(String.format("%s: %s", phase, message));
  }
}
