package io.dropwizard.health.response;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public interface HealthResponseProvider {
    @Nonnull
    HealthResponse fullHealthResponse(@Nullable String type);

    @Nonnull
    HealthResponse minimalHealthResponse(@Nullable String type);

    @Nonnull
    HealthResponse partialHealthResponse(@Nullable String type, @Nonnull Collection<String> names);
}
