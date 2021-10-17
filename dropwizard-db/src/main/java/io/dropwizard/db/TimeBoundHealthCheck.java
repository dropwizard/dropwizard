package io.dropwizard.db;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;

import java.lang.InterruptedException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public class TimeBoundHealthCheck {
    private final ExecutorService executorService; 
    private final Duration duration;
     
    public TimeBoundHealthCheck(ExecutorService executorService, Duration duration) {
        this.executorService = executorService;
        this.duration = duration;
    }
    
    public HealthCheck.Result check(Callable<HealthCheck.Result> c) {
        try {
            return executorService.submit(c).get(duration.getQuantity(), duration.getUnit());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception ignored) {
        }
        return unhealthy();
    }

    private HealthCheck.Result unhealthy() {
        return HealthCheck.Result.unhealthy("Unable to successfully check in %s", duration);
    }
}
