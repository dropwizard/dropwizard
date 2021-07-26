package io.dropwizard.health;

import io.dropwizard.health.conf.HealthCheckType;

import javax.annotation.Nonnull;
import java.util.Objects;

class DelegatingHealthStateView implements HealthStateView, Comparable<DelegatingHealthStateView> {
    @Nonnull
    private final ScheduledHealthCheck scheduledHealthCheck;

    public DelegatingHealthStateView(@Nonnull final ScheduledHealthCheck scheduledHealthCheck) {
        this.scheduledHealthCheck = Objects.requireNonNull(scheduledHealthCheck);
    }

    @Nonnull
    @Override
    public String getName() {
        return scheduledHealthCheck.getName();
    }

    @Override
    public boolean isHealthy() {
        return scheduledHealthCheck.isHealthy();
    }

    @Nonnull
    @Override
    public HealthCheckType getType() {
        return scheduledHealthCheck.getType();
    }

    @Override
    public boolean isCritical() {
        return scheduledHealthCheck.isCritical();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DelegatingHealthStateView)) return false;
        DelegatingHealthStateView that = (DelegatingHealthStateView) o;
        return isHealthy() == that.isHealthy() && getName().equals(that.getName()) && getType() == that.getType()
                && isCritical() == that.isCritical();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), isHealthy(), getType(), isCritical());
    }

    @Override
    public String toString() {
        return "HealthStateView{" +
                "name='" + scheduledHealthCheck.getName() + '\'' +
                ", healthy=" + scheduledHealthCheck.isHealthy() +
                ", type=" + scheduledHealthCheck.getType() +
                ", critical=" + scheduledHealthCheck.isCritical() +
                '}';
    }

    @Override
    public int compareTo(final DelegatingHealthStateView other) {
        return this.getName().compareTo(other.getName());
    }
}
