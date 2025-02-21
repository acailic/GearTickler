package com.geartickler.service;

import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import com.geartickler.model.AIWorkload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class AIWorkloadService {

  private static final Logger log = LoggerFactory.getLogger(AIWorkloadService.class);
  private final KubernetesClient kubernetesClient;

  @Inject
  public AIWorkloadService(KubernetesClient kubernetesClient) {
    this.kubernetesClient = kubernetesClient;
  }

  public Service createOrUpdateService(AIWorkload resource) {
    Service service = new ServiceBuilder()
        .withNewMetadata()
        .withName(resource.getMetadata().getName())
        .withNamespace(resource.getMetadata().getNamespace())
        .addToLabels("app", resource.getMetadata().getName())
        .endMetadata()
        .withNewSpec()
        .addNewPort()
        .withPort(80)
        .withTargetPort(8080)
        .withName("http")
        .endPort()
        .addToSelector("app", resource.getMetadata().getName())
        .withType("ClusterIP")
        .endSpec()
        .build();

    log.info("Creating/updating service for AIWorkload: {}", resource.getMetadata().getName());

    return kubernetesClient.services()
        .inNamespace(resource.getMetadata().getNamespace())
        .createOrReplace(service);
  }

  public void deleteService(String namespace, String name) {
    log.info("Deleting service: {} in namespace: {}", name, namespace);
    kubernetesClient.services()
        .inNamespace(namespace)
        .withName(name)
        .delete();
  }
}
