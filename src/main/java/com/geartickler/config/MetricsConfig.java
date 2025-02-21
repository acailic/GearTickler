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
}
