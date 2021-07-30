package io.dropwizard.health.response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public final class HealthResponse {
    private final boolean healthy;
    @Nullable
    private final String message;
    @Nonnull
    private final String contentType;

    HealthResponse(boolean healthy, @Nullable final String message, @Nonnull final String contentType) {
        this.healthy = healthy;
        this.message = message;
        this.contentType = Objects.requireNonNull(contentType);
    }

    public boolean isHealthy() {
        return healthy;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nonnull
    public String getContentType() {
        return contentType;
    }
}

