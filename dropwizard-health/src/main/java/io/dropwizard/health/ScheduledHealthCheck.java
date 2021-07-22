package io.dropwizard.health;

import com.codahale.metrics.Counter;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.health.conf.HealthCheckType;
import io.dropwizard.health.conf.Schedule;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ScheduledHealthCheck implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(ScheduledHealthCheck.class);
    private final String name;
    private final HealthCheckType type;
    private final boolean critical;
    private final HealthCheck healthCheck;
    private final Schedule schedule;
    private final State state;
    private final Counter healthyCheckCounter;
    private final Counter unhealthyCheckCounter;

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

    @Override
    public void run() {
        log.trace("executing health check: name={}", name);

        HealthCheck.Result result;
        try {
           result = healthCheck.execute();
        } catch (final Exception e) {
            log.warn("Check for name={} failed exceptionally", name, e);
            result = HealthCheck.Result.unhealthy(e);
        }

        if (result.isHealthy()) {
            log.trace("health check result: name={} result=success", name);
            state.success();
            healthyCheckCounter.inc();
        } else {
            log.trace("health check result: name={} result=failure result={}", name, result);
            state.failure();
            unhealthyCheckCounter.inc();
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof ScheduledHealthCheck)) return false;
        final ScheduledHealthCheck that = (ScheduledHealthCheck) o;
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
        sb.append(", healthyCheckCounter=").append(healthyCheckCounter);
        sb.append(", unhealthyCheckCounter=").append(unhealthyCheckCounter);
        sb.append('}');
        return sb.toString();
    }
}
