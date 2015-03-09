package io.dropwizard.db;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;

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
        HealthCheck.Result result;
        try {
            result = executorService.submit(c).get(duration.getQuantity(), duration.getUnit());
        } catch (Exception e) {
            result = HealthCheck.Result.unhealthy("Unable to successfully check in %s", duration);
        }
        return result;
    }
}
