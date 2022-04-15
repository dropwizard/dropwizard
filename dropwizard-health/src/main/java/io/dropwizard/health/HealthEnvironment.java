package io.dropwizard.health;

import com.codahale.metrics.health.HealthCheckRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class HealthEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthEnvironment.class);

    @NotNull
    private final HealthCheckRegistry healthCheckRegistry;
    @NotNull
    private final Collection<HealthStateListener> healthStateListeners;
    @Nullable
    private HealthStateAggregator healthStateAggregator;

    public HealthEnvironment(final HealthCheckRegistry healthCheckRegistry) {
        this.healthCheckRegistry = Objects.requireNonNull(healthCheckRegistry);
        this.healthStateListeners = new ArrayList<>();
    }

    public void addHealthStateListener(final HealthStateListener listener) {
        healthStateListeners.add(listener);
    }

    @NotNull
    public HealthCheckRegistry healthChecks() {
        return healthCheckRegistry;
    }

    @NotNull
    public Collection<HealthStateListener> healthStateListeners() {
        return healthStateListeners;
    }

    @NotNull
    public HealthStateAggregator healthStateAggregator() {
        if (healthStateAggregator == null) {
            final String message = "Cannot access the HealthStateAggregator before HealthFactory setup has occurred";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        return healthStateAggregator;
    }

    void setHealthStateAggregator(@NotNull final HealthStateAggregator healthStateAggregator) {
        this.healthStateAggregator = Objects.requireNonNull(healthStateAggregator);
    }
}
