package com.geartickler.operator.controller;

import com.geartickler.operator.model.AIWorkload;
import com.geartickler.operator.model.AIWorkloadSpec;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AIWorkloadControllerTest {

  private KubernetesServer server;
  private KubernetesClient client;
  private AIWorkloadController controller;

  @BeforeEach
  void setUp() {
    // Initialize the server
    server = new KubernetesServer(true, true);
    server.before();

    // Initialize the client from the server
    client = server.getClient();

    // Initialize the controller and inject the client
    controller = new AIWorkloadController();
    controller.client = client;

    // Create the default namespace
    client.namespaces().create(new NamespaceBuilder()
        .withNewMetadata()
        .withName("default")
        .endMetadata()
        .build());
  }

  @Test
  void testReconcileCreatesDeployment() {
    // Arrange
    AIWorkload workload = new AIWorkload();
    workload.setMetadata(new io.fabric8.kubernetes.api.model.ObjectMeta());
    workload.getMetadata().setNamespace("default");
    workload.getMetadata().setName("test-model");
    workload.setSpec(new AIWorkloadSpec());
    workload.getSpec().setModelName("test-model");
    workload.getSpec().setModelVersion("v1");
    workload.getSpec().setReplicas(1);

    Context<AIWorkload> context = Mockito.mock(Context.class);

    // Act
    UpdateControl<AIWorkload> result = controller.reconcile(workload, context);

    // Assert
    Deployment deployment = client.apps().deployments().inNamespace("default").withName("test-model").get();
    assertNotNull(deployment, "Deployment should be created");
    assertEquals("test-model", deployment.getMetadata().getName());
    assertEquals(1, deployment.getSpec().getReplicas());
  }
}
