package io.dropwizard.health.response;

import io.dropwizard.health.HealthStatusChecker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class SimpleHealthResponseProvider implements HealthResponseProvider {

    @Nonnull
    private final HealthStatusChecker healthStatusChecker;
    @Nonnull
    private final String contentType;
    @Nonnull
    private final String healthyValue;
    @Nonnull
    private final String unhealthyValue;

    public SimpleHealthResponseProvider(@Nonnull final HealthStatusChecker healthStatusChecker,
                                        @Nonnull final String contentType, @Nonnull final String healthyValue,
                                        @Nonnull final String unhealthyValue) {
        this.healthStatusChecker = Objects.requireNonNull(healthStatusChecker);
        this.contentType = Objects.requireNonNull(contentType);
        this.healthyValue = Objects.requireNonNull(healthyValue);
        this.unhealthyValue = Objects.requireNonNull(unhealthyValue);
    }

    @Nonnull
    @Override
    public HealthResponse currentHealthResponse(@Nullable final String type) {
        final boolean healthy = healthStatusChecker.isHealthy(type);
        final String message = healthy ? healthyValue : unhealthyValue;
        return new HealthResponse(healthy, message, contentType);
    }

    @Nonnull
    @Override
    public String contentType() {
        return contentType;
    }
}
