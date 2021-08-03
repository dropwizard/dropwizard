package io.dropwizard.health.response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

public abstract class HealthResponse {
    protected final boolean healthy;
    @Nullable
    protected final String message;
    @Nonnull
    protected final String contentType;

    protected HealthResponse(boolean healthy, @Nullable final String message, @Nonnull final String contentType) {
        this.healthy = healthy;
        this.message = message;
        this.contentType = Objects.requireNonNull(contentType);
    }

    public boolean isHealthy() {
        return healthy;
    }

    @Nonnull
    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    @Nonnull
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthResponse)) return false;
        HealthResponse that = (HealthResponse) o;
        return healthy == that.healthy && Objects.equals(message, that.message) && contentType.equals(that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(healthy, message, contentType);
    }

    @Override
    public String toString() {
        return "HealthResponse{" +
            "healthy=" + healthy +
            ", message='" + message + '\'' +
            ", contentType='" + contentType + '\'' +
            '}';
    }
}

