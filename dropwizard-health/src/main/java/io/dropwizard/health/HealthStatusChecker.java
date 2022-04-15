package io.dropwizard.health;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface HealthStatusChecker {
    default boolean isHealthy() {
        return isHealthy(null);
    }

    boolean isHealthy(@Nullable final String type);
}
