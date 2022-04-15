package io.dropwizard.health.response;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Map;

public interface HealthResponseProvider {
    @NonNull
    HealthResponse healthResponse(Map<String, Collection<String>> queryParams);
}
