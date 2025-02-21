package com.geartickler.operator.controller;

import com.geartickler.operator.model.AIWorkload;
import com.geartickler.operator.model.AIWorkloadSpec;
import com.geartickler.operator.model.AIWorkloadStatus;
import com.geartickler.operator.model.AIWorkloadCondition;
import com.geartickler.operator.model.ResourceMetrics;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.ControllerConfiguration;
import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@RegisterForReflection
@ControllerConfiguration
public class AIWorkloadController implements Reconciler<AIWorkload> {

  private static final Logger log = LoggerFactory.getLogger(AIWorkloadController.class);

  @Inject
  KubernetesClient client;

  @Override
  public UpdateControl<AIWorkload> reconcile(AIWorkload resource, Context<AIWorkload> context) {
    log.info("Reconciling AIWorkload: {}", resource.getMetadata().getName());

    try {
      // Create or update the deployment
      Deployment deployment = createDeployment(resource);
      Resource<Deployment> deploymentResource = client.apps().deployments()
          .inNamespace(resource.getMetadata().getNamespace())
          .withName(resource.getMetadata().getName());

      if (deploymentResource.get() == null) {
        client.apps().deployments().create(deployment);
        log.info("Created deployment for AIWorkload: {}", resource.getMetadata().getName());
      } else {
        client.apps().deployments().createOrReplace(deployment);
        log.info("Updated deployment for AIWorkload: {}", resource.getMetadata().getName());
      }

      // Update status
      updateStatus(resource);

      return UpdateControl.patchStatus(resource);
    } catch (Exception e) {
      log.error("Error reconciling AIWorkload: {}", e.getMessage(), e);
      updateStatusWithError(resource, e);
      return UpdateControl.patchStatus(resource);
    }
  }

  private Deployment createDeployment(AIWorkload resource) {
    AIWorkloadSpec spec = resource.getSpec();

    return new DeploymentBuilder()
        .withNewMetadata()
        .withName(resource.getMetadata().getName())
        .withNamespace(resource.getMetadata().getNamespace())
        .addToLabels("app", resource.getMetadata().getName())
        .addToLabels("aiworkload", resource.getMetadata().getName())
        .endMetadata()
        .withNewSpec()
        .withReplicas(spec.getReplicas())
        .withNewSelector()
        .addToMatchLabels("app", resource.getMetadata().getName())
        .endSelector()
        .withNewTemplate()
        .withNewMetadata()
        .addToLabels("app", resource.getMetadata().getName())
        .endMetadata()
        .withNewSpec()
        .addNewContainer()
        .withName(resource.getMetadata().getName())
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
        .endContainer()
        .endSpec()
        .endTemplate()
        .endSpec()
        .build();
  }

  private String getModelImage(AIWorkloadSpec spec) {
    // In a real implementation, this would be more sophisticated
    return String.format("ai-models/%s:%s", spec.getModelName(), spec.getModelVersion());
  }

  private void updateStatus(AIWorkload resource) {
    Deployment deployment = client.apps().deployments()
        .inNamespace(resource.getMetadata().getNamespace())
        .withName(resource.getMetadata().getName())
        .get();

    AIWorkloadStatus status = new AIWorkloadStatus();
    if (deployment != null && deployment.getStatus() != null) {
      status.setAvailableReplicas(deployment.getStatus().getAvailableReplicas());
      status.setReadyReplicas(deployment.getStatus().getReadyReplicas());
    } else {
      status.setAvailableReplicas(0);
      status.setReadyReplicas(0);
    }
    resource.setStatus(status);
  }

  private void updateStatusWithError(AIWorkload resource, Exception e) {
    AIWorkloadStatus status = resource.getStatus();
    if (status == null) {
      status = new AIWorkloadStatus();
    }

    status.setPhase("Error");

    AIWorkloadCondition condition = new AIWorkloadCondition();
    condition.setType("Error");
    condition.setStatus("True");
    condition.setLastTransitionTime(ZonedDateTime.now().format(DateTimeFormatter.ISO_INSTANT));
    condition.setReason("ReconciliationError");
    condition.setMessage(e.getMessage());

    status.getConditions().add(condition);
    resource.setStatus(status);
  }
}
