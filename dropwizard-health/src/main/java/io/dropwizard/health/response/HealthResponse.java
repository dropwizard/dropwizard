package io.dropwizard.health.response;

import io.dropwizard.health.HealthStateView;
import java.util.Collection;
import java.util.Objects;
import javax.annotation.Nonnull;

public final class HealthResponse {
    private final boolean healthy;
    @Nonnull
    private final String message;
    @Nonnull
    private final String contentType;
    @Nonnull
    private final Collection<HealthStateView> views;

    public HealthResponse(boolean healthy, @Nonnull final String message, @Nonnull final String contentType,
                          final Collection<HealthStateView> views) {
        this.healthy = healthy;
        this.message = message;
        this.contentType = Objects.requireNonNull(contentType);
        this.views = Objects.requireNonNull(views);
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

    @Nonnull
    public Collection<HealthStateView> getViews() {
        return views;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthResponse)) return false;
        HealthResponse that = (HealthResponse) o;
        return healthy == that.healthy && Objects.equals(message, that.message) && contentType.equals(that.contentType) && views.equals(that.views);
    }

    @Override
    public int hashCode() {
        return Objects.hash(healthy, message, contentType, views);
    }

    @Override
    public String toString() {
        return "HealthResponse{" +
                "healthy=" + healthy +
                ", message='" + message + '\'' +
                ", contentType='" + contentType + '\'' +
                ", views=" + views +
                '}';
    }
}

