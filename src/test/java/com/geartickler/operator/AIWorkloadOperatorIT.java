package com.geartickler.operator;

import com.geartickler.operator.model.AIWorkload;
import com.geartickler.operator.model.AIWorkloadSpec;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import io.javaoperatorsdk.operator.Operator;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
class AIWorkloadOperatorIT {

  @Inject
  KubernetesClient client;

  @Inject
  Operator operator;

  @BeforeEach
  void setUp() {
    // Create a test namespace
    client.namespaces()
        .create(new NamespaceBuilder().withNewMetadata().withName("test-namespace").endMetadata().build());
  }

  @Test
  void testOperatorDeploysWorkload() {
    // Arrange
    AIWorkload workload = new AIWorkload();
    workload.setMetadata(new io.fabric8.kubernetes.api.model.ObjectMeta());
    workload.getMetadata().setName("test-workload");
    workload.getMetadata().setNamespace("test-namespace");
    workload.setSpec(new AIWorkloadSpec());
    workload.getSpec().setModelName("test-model");
    workload.getSpec().setModelVersion("v1");
    workload.getSpec().setReplicas(1);

    // Act
    client.resource(workload).inNamespace("test-namespace").createOrReplace();
    operator.start();

    // Assert
    Deployment deployment = client.apps().deployments().inNamespace("test-namespace").withName("test-workload").get();
    assertNotNull(deployment, "Deployment should be created by the operator");
  }
}
