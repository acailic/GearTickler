package com.geartickler.service;

import ai.onnxruntime.*;
import com.geartickler.model.AIWorkload;
import com.geartickler.model.AIWorkloadStatus.ModelStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

@ApplicationScoped
public class ONNXModelService {
  private static final Logger log = LoggerFactory.getLogger(ONNXModelService.class);
  private final Map<String, OrtSession> modelSessions;
  private final Map<String, ModelMetadata> modelMetadata;
  private final Set<String> supportedDevices;

  @Inject
  public ONNXModelService() {
    this.modelSessions = new ConcurrentHashMap<>();
    this.modelMetadata = new ConcurrentHashMap<>();
    this.supportedDevices = initializeSupportedDevices();
  }

  private Set<String> initializeSupportedDevices() {
    Set<String> devices = new HashSet<>();
    devices.add("CPU");

    try {
      if (OrtEnvironment.getEnvironment().getAvailableProviders().contains("CUDA")) {
        devices.add("CUDA");
      }
      if (OrtEnvironment.getEnvironment().getAvailableProviders().contains("TensorRT")) {
        devices.add("TensorRT");
      }
    } catch (OrtException e) {
      log.error("Error checking available providers", e);
    }

    return devices;
  }

  public void loadModel(AIWorkload workload, Path modelPath) {
    String key = getModelKey(workload);
    ModelStatus modelStatus = workload.getStatus().getModel();

    try {
      OrtEnvironment env = OrtEnvironment.getEnvironment();
      OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();

      // Configure execution providers based on available hardware
      if (supportedDevices.contains("TensorRT")) {
        sessionOptions.addTensorrt();
      } else if (supportedDevices.contains("CUDA")) {
        sessionOptions.addCUDA();
      }

      // Enable graph optimization
      sessionOptions.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.BASIC_OPT);
      sessionOptions.setIntraOpNumThreads(4);
      sessionOptions.setInterOpNumThreads(2);

      // Load the model
      OrtSession session = env.createSession(modelPath.toString(), sessionOptions);
      modelSessions.put(key, session);

      // Extract and store model metadata
      ModelMetadata metadata = extractModelMetadata(session);
      modelMetadata.put(key, metadata);

      // Update model status
      updateModelStatus(modelStatus, metadata);

      log.info("Successfully loaded ONNX model for {}", key);
    } catch (OrtException e) {
      log.error("Failed to load ONNX model for {}", key, e);
      modelStatus.setState("Failed");
      modelStatus.setHealthStatus("Error: " + e.getMessage());
    }
  }

  private ModelMetadata extractModelMetadata(OrtSession session) throws OrtException {
    ModelMetadata metadata = new ModelMetadata();

    metadata.inputNames = session.getInputNames();
    metadata.inputInfo = session.getInputInfo();
    metadata.outputNames = session.getOutputNames();
    metadata.modelMetadata = session.getMetadata();

    return metadata;
  }

  private void updateModelStatus(ModelStatus status, ModelMetadata metadata) {
    status.setState("Loaded");
    status.setHealthStatus("Healthy");
    status.setFormat("ONNX");
    status.setSupportedDevices(new ArrayList<>(supportedDevices));

    Map<String, String> properties = new HashMap<>();
    properties.put("inputNames", String.join(",", metadata.inputNames));
    properties.put("outputNames", String.join(",", metadata.outputNames));
    if (metadata.modelMetadata != null) {
      properties.put("description", metadata.modelMetadata.getDescription());
      properties.put("domain", metadata.modelMetadata.getDomain());
      properties.put("graphName", metadata.modelMetadata.getGraphName());
      properties.put("version", String.valueOf(metadata.modelMetadata.getVersion()));
    }
    status.setModelProperties(properties);
  }

  public OrtSession getSession(AIWorkload workload) {
    return modelSessions.get(getModelKey(workload));
  }

  public ModelMetadata getMetadata(AIWorkload workload) {
    return modelMetadata.get(getModelKey(workload));
  }

  public void unloadModel(AIWorkload workload) {
    String key = getModelKey(workload);
    OrtSession session = modelSessions.remove(key);
    if (session != null) {
      try {
        session.close();
        modelMetadata.remove(key);
        log.info("Successfully unloaded ONNX model for {}", key);
      } catch (OrtException e) {
        log.error("Error closing ONNX session for {}", key, e);
      }
    }
  }

  public boolean isModelLoaded(AIWorkload workload) {
    return modelSessions.containsKey(getModelKey(workload));
  }

  private String getModelKey(AIWorkload workload) {
    return workload.getMetadata().getNamespace() + "/" + workload.getMetadata().getName();
  }

  private static class ModelMetadata {
    Set<String> inputNames;
    Map<String, NodeInfo> inputInfo;
    Set<String> outputNames;
    OrtModelMetadata modelMetadata;
  }

  @PreDestroy
  public void cleanup() {
    modelSessions.values().forEach(session -> {
      try {
        session.close();
      } catch (OrtException e) {
        log.error("Error closing ONNX session during cleanup", e);
      }
    });
    modelSessions.clear();
    modelMetadata.clear();
  }
}
