# AI Workload Operator

A Kubernetes operator for managing AI/ML workloads, built with Quarkus and Java Operator SDK.

## Features

- Dynamic resource scaling for AI workloads (training/inference)
- GPU-aware scheduling and monitoring
- Automated deployment and lifecycle management of AI models
- Custom metrics collection for model performance
- Integration with popular ML frameworks via ONNX

## Prerequisite

- Kubernetes cluster (v1.19+)
- NVIDIA GPU Operator (for GPU support)
- Java 17+ƒ
- Maven 3.8+
- Docker

## Installation

1. Build the operator:

   ```bash
   mvn clean package
   ```

2. Build the container image:

   ```bash
   mvn package -Dquarkus.container-image.build=true
   ```

3. Apply the CRD:

   ```bash
   kubectl apply -f src/main/resources/k8s/aiworkload-crd.yaml
   ```

4. Deploy the operator:
   ```bash
   kubectl apply -f target/kubernetes/kubernetes.yml
   ```

## Usage

1. Create an AI workload:

   ```bash
   kubectl apply -f src/main/resources/k8s/sample-aiworkload.yaml
   ```

2. Check the status:

   ```bash
   kubectl get aiworkloads
   ```

3. Monitor the workload:
   ```bash
   kubectl describe aiworkload bert-sentiment
   ```

## Configuration

The operator can be configured through the following environment variables:

- `QUARKUS_KUBERNETES_CLIENT_NAMESPACE`: Target namespace (default: "default")
- `QUARKUS_OPERATOR_SDK_NAMESPACES`: Watched namespaces (default: "")
- `QUARKUS_LOG_LEVEL`: Logging level (default: "INFO")

## Metrics

The operator exposes Prometheus metrics at `/metrics`, including:

- Number of managed AI workloads
- Resource utilization metrics
- Scaling events
- Model performance metrics

## Development

1. Run in dev mode:

   ```bash
   mvn quarkus:dev
   ```

2. Run tests:

   ```bash
   mvn test
   ```

3. Build native image:
   ```bash
   mvn package -Pnative
   ```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

### Commands

#### set global java version sdk to 23

```bash
sdk default  java 17.0.9-zulu
```

```bash
sdk default  java 23.0.2-zulu
```
