package io.dropwizard.health.response;

import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Map;

public interface HealthResponseProvider {
    @NonNull
    HealthResponse healthResponse(Map<String, Collection<String>> queryParams);
}
