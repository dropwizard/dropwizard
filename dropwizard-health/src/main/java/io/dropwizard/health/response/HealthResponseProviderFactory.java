package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStatusChecker;
import io.dropwizard.jackson.Discoverable;
/**
 * A factory for building an {@link HealthResponseProvider} instance used to provide responses to health check requests.
 *
 * @see SimpleHealthResponseProviderFactory
 * @see JerseyHealthResponderFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DetailedJsonHealthResponseProvider.class)
public interface HealthResponseProviderFactory extends Discoverable {

    /**
     * Configures a health responder for responding to health check requests (e.g. from load balancer).
     *
     * @param healthStatusChecker an interface that exposes the ability to check current status of health.
     * @param healthStateAggregator an interface that exposes the ability to check an aggregate view of all health
     *                              states.
     * @param mapper A Jackson object mapper to allow writing JSON responses (if needed).
     */
    HealthResponseProvider build(HealthStatusChecker healthStatusChecker, HealthStateAggregator healthStateAggregator,
                                 ObjectMapper mapper);
}
