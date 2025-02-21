package com.geartickler.operator;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class AIWorkloadOperator implements QuarkusApplication {

    private static final Logger log = LoggerFactory.getLogger(AIWorkloadOperator.class);

    public static void main(String... args) {
        Quarkus.run(AIWorkloadOperator.class, args);
    }

    @Override
    public int run(String... args) throws Exception {
        log.info("Starting AI Workload Operator");
        Quarkus.waitForExit();
        return 0;
    }
}
