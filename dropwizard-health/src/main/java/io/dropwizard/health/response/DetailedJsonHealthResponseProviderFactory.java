package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStatusChecker;

@JsonTypeName("detailed-json")
public class DetailedJsonHealthResponseProviderFactory implements HealthResponseProviderFactory {
    @Override
    public HealthResponseProvider build(final HealthStatusChecker healthStatusChecker,
                                        final HealthStateAggregator healthStateAggregator,
                                        final ObjectMapper mapper) {
        return new DetailedJsonHealthResponseProvider(healthStatusChecker, healthStateAggregator, mapper);
    }
}
