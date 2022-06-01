package io.dropwizard.health.response;

import java.util.Collection;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface HealthResponseProvider {
    @NonNull
    HealthResponse healthResponse(Map<String, Collection<String>> queryParams);
}
