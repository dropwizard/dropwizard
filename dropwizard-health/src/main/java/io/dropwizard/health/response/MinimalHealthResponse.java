package io.dropwizard.health.response;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class MinimalHealthResponse extends HealthResponse {
    MinimalHealthResponse(boolean healthy, @Nonnull String contentType) {
        super(healthy, null, contentType);
    }

    @Nonnull
    @Override
    public Optional<String> getMessage() {
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "MinimalHealthResponse{" +
            "healthy=" + healthy +
            ", message='" + message + '\'' +
            ", contentType='" + contentType + '\'' +
            '}';
    }
}
