package io.dropwizard.db;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;

public class HealthCheckResultCreator {
    public HealthCheck.Result unhealthy(String duration) {
        return HealthCheck.Result.unhealthy("Unable to successfully check in time limit: %s", duration);
    }
}
