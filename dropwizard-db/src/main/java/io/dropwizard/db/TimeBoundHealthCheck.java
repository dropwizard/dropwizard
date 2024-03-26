package io.dropwizard.db;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class TimeBoundHealthCheck {
    private final ExecutorService executorService;
    private final Duration duration;
    private final HealthCheckResultCreator resultCreator = new HealthCheckResultCreator();

    public TimeBoundHealthCheck(ExecutorService executorService, Duration duration) {
        this.executorService = executorService;
        this.duration = duration;
    }

    public HealthCheck.Result check(Callable<HealthCheck.Result> callable) {
        try {
            return executorService.submit(callable).get(duration.getQuantity(), duration.getUnit());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return resultCreator.unhealthy(duration.toString());
        } catch (Exception ignored) {
            return resultCreator.unhealthy(duration.toString());
        }
    }
}

