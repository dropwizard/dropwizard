package com.example.health;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.concurrent.atomic.AtomicBoolean;

public class HealthApp extends Application<Configuration> {
    static final String CRITICAL_HEALTH_CHECK_NAME_1 = "critical1";
    static final String CRITICAL_HEALTH_CHECK_NAME_2 = "critical2";
    static final String NON_CRITICAL_HEALTH_CHECK_NAME = "nonCritical";

    private final AtomicBoolean criticalCheckHealthy1 = new AtomicBoolean();
    private final AtomicBoolean criticalCheckHealthy2 = new AtomicBoolean();
    private final AtomicBoolean nonCriticalCheckHealthy = new AtomicBoolean();

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
    }

    @Override
    public void run(final Configuration configuration, final Environment environment) throws Exception {
        environment.healthChecks().register(CRITICAL_HEALTH_CHECK_NAME_1, new HealthCheck() {
            @Override
            protected Result check() {
                return criticalCheckHealthy1.get() ? Result.healthy() : Result.builder().unhealthy().build();
            }
        });

        environment.healthChecks().register(CRITICAL_HEALTH_CHECK_NAME_2, new HealthCheck() {
            @Override
            protected Result check() {
                return criticalCheckHealthy2.get() ? Result.healthy() : Result.builder().unhealthy().build();
            }
        });

        environment.healthChecks().register(NON_CRITICAL_HEALTH_CHECK_NAME, new HealthCheck() {
            @Override
            protected Result check() {
                return nonCriticalCheckHealthy.get() ? Result.healthy() : Result.builder().unhealthy().build();
            }
        });
    }

    AtomicBoolean getCriticalCheckHealthy1() {
        return criticalCheckHealthy1;
    }

    AtomicBoolean getCriticalCheckHealthy2() {
        return criticalCheckHealthy2;
    }

    AtomicBoolean getNonCriticalCheckHealthy() {
        return nonCriticalCheckHealthy;
    }
}
