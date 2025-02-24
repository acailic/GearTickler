package com.geartickler.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.time.Duration;

@ConfigMapping(prefix = "metrics")
public interface MetricsConfig {

  Prometheus prometheus();

  Dcgm dcgm();

  Collection collection();

  Scheduler scheduler();

  Scaling scaling();

  Resources resources();

  interface Prometheus {
    @WithDefault("http://prometheus-server:9090/api/v1/query")
    String url();

    @WithDefault("5s")
    Duration timeout();
  }

  interface Dcgm {
    @WithDefault("http://dcgm-exporter.default.svc.cluster.local:9400")
    String url();
  }

  interface Collection {
    @WithDefault("30s")
    Duration interval();
  }

  interface Scheduler {
    @WithDefault("2")
    int poolSize();
  }

  interface Scaling {
    @WithDefault("0.85")
    double gpuUtilizationThresholdHigh();

    @WithDefault("0.3")
    double gpuUtilizationThresholdLow();

    @WithDefault("100.0")
    double latencyThresholdMs();

    @WithDefault("100")
    double queueLengthThreshold();

    @WithDefault("300")
    int scalingCooldownSeconds();

    @WithDefault("0.9")
    double gpuMemoryThresholdHigh();

    @WithDefault("0.7")
    double targetGpuUtilization();

    @WithDefault("0.8")
    double targetMemoryUtilization();

    @WithDefault("0.8")
    double targetCpuUtilization();
  }

  interface Resources {
    @WithDefault("4.0")
    double baseMemoryGb();

    @WithDefault("2.0")
    double baseCpuCores();

    @WithDefault("1.0")
    double baseGpuUnits();

    @WithDefault("4")
    int intraOpThreads();

    @WithDefault("2")
    int interOpThreads();
  }
}
