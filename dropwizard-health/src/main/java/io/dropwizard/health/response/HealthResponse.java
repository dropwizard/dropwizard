package io.dropwizard.health.response;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class HealthResponse {
    private final boolean healthy;
    @Nonnull
    private final String message;
    @Nonnull
    private final String contentType;

    HealthResponse(boolean healthy, @Nonnull final String message, @Nonnull final String contentType) {
        this.healthy = healthy;
        this.message = Objects.requireNonNull(message);
        this.contentType = Objects.requireNonNull(contentType);
    }

    public boolean isHealthy() {
        return healthy;
    }

    @Nonnull
    public String getMessage() {
        return message;
    }

    @Nonnull
    public String getContentType() {
        return contentType;
    }
}

