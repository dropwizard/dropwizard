package io.dropwizard.health.response;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Map;

public interface HealthResponseProvider {
    @Nonnull
    HealthResponse healthResponse(Map<String, Collection<String>> queryParams);
}
