package io.dropwizard.health.response;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

public interface HealthResponseProvider {
    @NotNull
    HealthResponse healthResponse(Map<String, Collection<String>> queryParams);
}
