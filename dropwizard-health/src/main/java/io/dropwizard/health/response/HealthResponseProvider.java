package io.dropwizard.health.response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface HealthResponseProvider {
    @Nonnull
    HealthResponse currentHealthResponse(@Nullable String type);

    @Nonnull
    String contentType();
}
