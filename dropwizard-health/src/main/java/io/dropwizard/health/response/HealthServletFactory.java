package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStatusChecker;
import io.dropwizard.jackson.Discoverable;

import javax.servlet.http.HttpServlet;

/**
 * A factory for building an {@link HttpServlet} instance used for responding to health check requests.
 *
 * @see DefaultHealthServletFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultHealthServletFactory.class)
public interface HealthServletFactory extends Discoverable {
    /**
     * Build a servlet for responding to health check requests (e.g. from load balancer).
     *
     * @param healthStatusChecker an interface that exposes the ability to check current status of health.
     * @param healthStateAggregator an interface that exposes the ability to check an aggregate view of all health
     *                              states.
     * @param mapper A Jackson object mapper to allow writing JSON responses (if needed).
     * @return a {@link HttpServlet} that responds to health check requests
     */
    HttpServlet build(HealthStatusChecker healthStatusChecker, HealthStateAggregator healthStateAggregator,
                      ObjectMapper mapper);
}
