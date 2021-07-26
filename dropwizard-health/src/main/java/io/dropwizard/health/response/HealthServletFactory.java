package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
     * @return a {@link HttpServlet} that responds to health check requests
     */
    HttpServlet build(final HealthStatusChecker healthStatusChecker);
}
