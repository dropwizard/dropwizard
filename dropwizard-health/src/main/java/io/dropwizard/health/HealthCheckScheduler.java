package io.dropwizard.health;

import io.dropwizard.util.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthCheckScheduler {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckScheduler.class);

    private final ScheduledExecutorService executorService;
    private final Map<String, ScheduledFuture> futures = new ConcurrentHashMap<>();

    public HealthCheckScheduler(final ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    void scheduleInitial(final ScheduledHealthCheck check) {
        final Duration interval;
        if (check.isHealthy()) {
            interval = check.getSchedule().getCheckInterval();
        } else {
            interval = check.getSchedule().getDowntimeInterval();
        }

        schedule(check, check.getSchedule().getInitialDelay(), interval);
    }

    public void schedule(final ScheduledHealthCheck check, final boolean healthy) {
        unschedule(check.getName());

        final Duration interval;
        if (healthy) {
            interval = check.getSchedule().getCheckInterval();
        } else {
            interval = check.getSchedule().getDowntimeInterval();
        }

        schedule(check, interval, interval);
    }

    private void schedule(final ScheduledHealthCheck check, final Duration initialDelay, final Duration delay) {
        final ScheduledFuture taskFuture = executorService.scheduleWithFixedDelay(check, initialDelay.toMilliseconds(),
                delay.toMilliseconds(), TimeUnit.MILLISECONDS);
        futures.put(check.getName(), taskFuture);
        log.debug("Scheduled check: check={}", check);
    }

    public void unschedule(final String name) {
        final ScheduledFuture taskFuture = futures.get(name);
        if (taskFuture != null) {
            taskFuture.cancel(true);
            futures.remove(name);
            log.debug("Unscheduled check: name={}", name);
        }
    }
}
