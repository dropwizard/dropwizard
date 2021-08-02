package io.dropwizard.health;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.health.response.HealthResponder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class HealthEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthEnvironment.class);

    @Nonnull
    private final HealthCheckRegistry healthCheckRegistry;
    @Nonnull
    private final Collection<HealthStateListener> healthStateListeners;
    @Nullable
    private HealthStateAggregator healthStateAggregator;
    @Nullable
    private HealthResponder healthResponder;

    public HealthEnvironment(final HealthCheckRegistry healthCheckRegistry) {
        this.healthCheckRegistry = Objects.requireNonNull(healthCheckRegistry);
        this.healthStateListeners = new ArrayList<>();
    }

    public void addHealthStateListener(final HealthStateListener listener) {
        healthStateListeners.add(listener);
    }

    @Nonnull
    public HealthCheckRegistry healthChecks() {
        return healthCheckRegistry;
    }

    @Nonnull
    public Collection<HealthStateListener> healthStateListeners() {
        return healthStateListeners;
    }

    @Nonnull
    public HealthStateAggregator healthStateAggregator() {
        if (healthStateAggregator == null) {
            final String message = "Cannot access the HealthStateAggregator before HealthFactory setup has occurred";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        return healthStateAggregator;
    }

    void setHealthStateAggregator(@Nonnull final HealthStateAggregator healthStateAggregator) {
        this.healthStateAggregator = Objects.requireNonNull(healthStateAggregator);
    }

    @Nonnull
    public HealthResponder healthResponder() {
        if (healthResponder == null) {
            final String message = "Cannot access the HealthResponder before HealthFactory setup has occurred";
            LOGGER.error(message);
            throw new IllegalStateException(message);
        }
        return healthResponder;
    }

    public void setHealthResponder(@Nonnull final HealthResponder healthResponder) {
        this.healthResponder = Objects.requireNonNull(healthResponder);
    }
}
