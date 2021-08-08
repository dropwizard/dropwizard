package io.dropwizard.health.response;

import java.util.Collection;
import java.util.Map;
import javax.annotation.Nonnull;

public interface HealthResponseProvider {
    @Nonnull
    HealthResponse healthResponse(Map<String, Collection<String>> queryParams);
}
