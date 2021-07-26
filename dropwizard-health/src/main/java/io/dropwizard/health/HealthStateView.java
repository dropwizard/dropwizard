package io.dropwizard.health;

import io.dropwizard.health.conf.HealthCheckType;
import javax.annotation.Nonnull;

public interface HealthStateView {
    @Nonnull
    String getName();

    boolean isHealthy();

    @Nonnull
    HealthCheckType getType();

    boolean isCritical();
}
