package io.dropwizard.health;

import org.jetbrains.annotations.Nullable;

public interface HealthStatusChecker {
    default boolean isHealthy() {
        return isHealthy(null);
    }

    boolean isHealthy(@Nullable final String type);
}
