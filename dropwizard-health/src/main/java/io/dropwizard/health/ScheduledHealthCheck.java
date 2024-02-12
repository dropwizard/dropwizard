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
        if (!(o instanceof ScheduledHealthCheck that)) return false;
        return critical == that.critical &&
            Objects.equals(name, that.name) &&
            Objects.equals(healthCheck, that.healthCheck) &&
            Objects.equals(schedule, that.schedule) &&
            Objects.equals(state, that.state) &&
            Objects.equals(healthyCheckCounter, that.healthyCheckCounter) &&
            Objects.equals(unhealthyCheckCounter, that.unhealthyCheckCounter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, critical, healthCheck, schedule, state, healthyCheckCounter, unhealthyCheckCounter);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScheduledHealthCheck{");
        sb.append("name='").append(name).append('\'');
        sb.append(", critical=").append(critical);
        sb.append(", healthCheck=").append(healthCheck);
        sb.append(", schedule=").append(schedule);
        sb.append(", state=").append(state);
        sb.append(", healthyCheckCounter=").append(getCounterString(healthyCheckCounter));
        sb.append(", unhealthyCheckCounter=").append(getCounterString(unhealthyCheckCounter));
        sb.append(", previouslyRecovered=").append(previouslyRecovered);
        sb.append('}');
        return sb.toString();
    }

    private String getCounterString(Counter counter) {
        return Counter.class.equals(counter.getClass()) ? String.valueOf(counter.getCount()) : counter.toString();
    }
}
