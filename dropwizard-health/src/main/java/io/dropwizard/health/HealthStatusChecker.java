package io.dropwizard.health;

import javax.annotation.Nullable;

public interface HealthStatusChecker {
    default boolean isHealthy() {
        return isHealthy(null);
    }

    boolean isHealthy(@Nullable final String type);
}
