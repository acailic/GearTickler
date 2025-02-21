### Core Components

1.1. AIWorkload CRD

Purpose: Define the custom resource for AI workloads.

Fields:

- modelName: Name of the AI model.
- modelVersion: Version of the model.
- framework: AI framework (e.g., ONNX, TensorFlow).
- replicas: Number of replicas.
- resources: Resource requirements (CPU, GPU, memory).
- inferenceEndpoint: Endpoint for model inference.
- autoscaling: Autoscaling configuration.

### Controller

Purpose: Reconcile AI workloads and manage their lifecycle.

Key Responsibilities:

- Create/update Kubernetes Deployments for AI models.
- Monitor and update the status of AI workloads.
- Handle autoscaling based on resource utilization.
- Manage error conditions and retries.

### Metrics Collector

Purpose: Collect and expose metrics for AI workloads.
Metrics:
Resource utilization (CPU, GPU, memory).
Model performance (latency, throughput).
Scaling events and status.
