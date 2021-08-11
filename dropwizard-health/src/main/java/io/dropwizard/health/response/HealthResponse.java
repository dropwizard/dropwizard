package io.dropwizard.health.response;

import javax.annotation.Nonnull;
import java.util.Objects;

public final class HealthResponse {
    private final boolean healthy;
    @Nonnull
    private final String message;
    @Nonnull
    private final String contentType;
    private final int status;

    public HealthResponse(boolean healthy, @Nonnull final String message, @Nonnull final String contentType,
                          int status) {
        this.healthy = healthy;
        this.message = Objects.requireNonNull(message);
        this.contentType = Objects.requireNonNull(contentType);
        this.status = status;
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

    public int getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthResponse)) return false;
        HealthResponse that = (HealthResponse) o;
        return healthy == that.healthy && status == that.status && message.equals(that.message) && contentType.equals(that.contentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(healthy, message, contentType, status);
    }

    @Override
    public String toString() {
        return "HealthResponse{" +
            "healthy=" + healthy +
            ", message='" + message + '\'' +
            ", contentType='" + contentType + '\'' +
            ", status=" + status +
            '}';
    }
}

