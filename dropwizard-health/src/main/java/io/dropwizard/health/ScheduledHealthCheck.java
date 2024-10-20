package io.dropwizard.health;

import com.codahale.metrics.Counter;
import com.codahale.metrics.health.HealthCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

class ScheduledHealthCheck implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledHealthCheck.class);

    private final String name;
    private final HealthCheckType type;
    private final boolean critical;
    private final HealthCheck healthCheck;
    private final Schedule schedule;
    private final State state;
    private final Counter healthyCheckCounter;
    private final Counter unhealthyCheckCounter;
    private boolean previouslyRecovered = false;

    ScheduledHealthCheck(final String name,
                         final HealthCheckType type,
                         final boolean critical,
                         final HealthCheck healthCheck,
                         final Schedule schedule,
                         final State state,
                         final Counter healthyCheckCounter,
                         final Counter unhealthyCheckCounter) {
        this.name = Objects.requireNonNull(name);
        this.type = type;
        this.critical = critical;
        this.healthCheck = Objects.requireNonNull(healthCheck);
        this.schedule = Objects.requireNonNull(schedule);
        this.state = Objects.requireNonNull(state);
        this.healthyCheckCounter = Objects.requireNonNull(healthyCheckCounter);
        this.unhealthyCheckCounter = Objects.requireNonNull(unhealthyCheckCounter);
    }

    public String getName() {
        return name;
    }

    public HealthCheckType getType() {
        return type;
    }

    public boolean isCritical() {
        return critical;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public boolean isHealthy() {
        return state.getHealthy().get();
    }

    public boolean isPreviouslyRecovered() {
        return previouslyRecovered;
    }

    @Override
    public void run() {
        LOGGER.trace("executing health check: name={}", name);

        final boolean previousState = state.getHealthy().get();

        HealthCheck.Result result;
        try {
            result = healthCheck.execute();
        } catch (final Exception e) {
            LOGGER.warn("Check for name={} failed exceptionally", name, e);
            result = HealthCheck.Result.unhealthy(e);
        }

        if (result.isHealthy()) {
            LOGGER.trace("health check result: name={} result=success", name);
            state.success();
            healthyCheckCounter.inc();
            if (!previouslyRecovered && !previousState) {
                previouslyRecovered = true;
            }
        } else {
            LOGGER.trace("health check result: name={} result=failure result={}", name, result);
            state.failure();
            unhealthyCheckCounter.inc();
        }
    }

    public HealthStateView view() {
        return new HealthStateView(name, isHealthy(), type, isCritical());
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledHealthCheck)) return false;
        final ScheduledHealthCheck that = (ScheduledHealthCheck) o;
        return critical == that.critical &&
            previouslyRecovered == that.previouslyRecovered &&
            Objects.equals(name, that.name) &&
            type == that.type &&
            Objects.equals(healthCheck, that.healthCheck) &&
            Objects.equals(schedule, that.schedule) &&
            Objects.equals(state, that.state) &&
            Objects.equals(healthyCheckCounter, that.healthyCheckCounter) &&
            Objects.equals(unhealthyCheckCounter, that.unhealthyCheckCounter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, critical, healthCheck, schedule, state, healthyCheckCounter, unhealthyCheckCounter, previouslyRecovered);
    }

    @Override
    public String toString() {
        return "ScheduledHealthCheck{" +
            "name='" + name + '\'' +
            ", type=" + type +
            ", critical=" + critical +
            ", healthCheck=" + healthCheck +
            ", schedule=" + schedule +
            ", state=" + state +
            ", healthyCheckCounter=" + healthyCheckCounter +
            ", unhealthyCheckCounter=" + unhealthyCheckCounter +
            ", previouslyRecovered=" + previouslyRecovered +
            '}';
    }

    private String getCounterString(Counter counter) {
        return Counter.class.equals(counter.getClass()) ? String.valueOf(counter.getCount()) : counter.toString();
    }
}
