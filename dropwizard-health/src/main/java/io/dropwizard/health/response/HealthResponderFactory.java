package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;

import java.util.Collection;

/**
 * A factory for building an {@link HealthResponder} instance used for responding to health check requests.
 *
 * @see ServletHealthResponderFactory
 * @see JerseyHealthResponderFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = JerseyHealthResponderFactory.class)
public interface HealthResponderFactory extends Discoverable {
    /**
     * Configures a health responder for responding to health check requests (e.g. from load balancer).
     *
     * @param name The name of the application.
     * @param healthCheckUrlPaths The paths to expose a health check on.
     * @param healthResponseProvider A provider of responses to respond to requests with.
     * @param jersey The apps Jersey environment.
     * @param servlets The apps Servlet environment.
     * @param mapper A Jackson object mapper to allow writing JSON responses (if needed).
     */
    void configure(final String name, final Collection<String> healthCheckUrlPaths,
                   final HealthResponseProvider healthResponseProvider, final JerseyEnvironment jersey,
                   final ServletEnvironment servlets, final ObjectMapper mapper);
}
