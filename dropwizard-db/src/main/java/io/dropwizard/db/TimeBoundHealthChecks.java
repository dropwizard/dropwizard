package io.dropwizard.db;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class TimeBoundHealthChecks {
    public static HealthCheck.Result check(ExecutorService executorService, Duration timeBound, Callable<HealthCheck.Result> c) {
        HealthCheck.Result result;
        try {
            result = executorService.submit(c).get(timeBound.getQuantity(), timeBound.getUnit());
        } catch (Exception e) {
            result = HealthCheck.Result.unhealthy("Unable to successfully check in %s", timeBound);
        }
        return result;
    }
}
