package io.dropwizard.health;

import io.dropwizard.health.conf.HealthCheckType;

import javax.annotation.Nonnull;

public interface HealthStateView extends Comparable<HealthStateView> {
    @Nonnull
    String getName();

    boolean isHealthy();

    @Nonnull
    HealthCheckType getType();

    boolean isCritical();

    @Override
    default int compareTo(final HealthStateView other) {
        return this.getName().compareTo(other.getName());
    }
}
