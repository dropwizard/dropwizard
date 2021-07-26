package io.dropwizard.health;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public class HealthEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(LifecycleEnvironment.class);

    @Nonnull
    private final HealthCheckRegistry healthCheckRegistry;
    @Nullable
    private HealthStateListenerListener healthStateListenerListener;

    public HealthEnvironment(final HealthCheckRegistry healthCheckRegistry) {
        this.healthCheckRegistry = Objects.requireNonNull(healthCheckRegistry);
    }

    public void addHealthStateListener(final HealthStateListener listener) {
        if (healthStateListenerListener == null) {
            final String message = "Cannot add health state listener before HealthFactory setup has occurred";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        healthStateListenerListener.onHealthStateListenerAdded(listener);
    }

    @Nonnull
    public HealthCheckRegistry healthChecks() {
        return healthCheckRegistry;
    }

    Optional<HealthStateListenerListener> getHealthStateListenerListener() {
        return Optional.ofNullable(healthStateListenerListener);
    }

    void setHealthStateListenerListener(@Nonnull HealthStateListenerListener healthStateListenerListener) {
        this.healthStateListenerListener = Objects.requireNonNull(healthStateListenerListener);
    }
}
