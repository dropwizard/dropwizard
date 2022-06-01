package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthEnvironment;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;
import java.util.Collection;

/**
 * A factory for configuring a responder used for responding to health check requests.
 *
 * @see ServletHealthResponderFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = ServletHealthResponderFactory.class)
public interface HealthResponderFactory extends Discoverable {
    /**
     * Configures a health responder for responding to health check requests (e.g. from load balancer).
     *
     * @param name                   The name of the application.
     * @param healthCheckUrlPaths    The paths to expose a health check on.
     * @param healthResponseProvider A provider of responses to respond to requests with.
     * @param health                 The health environment.
     * @param jersey                 The Jersey environment.
     * @param servlets               The servlet environment.
     * @param mapper                 A Jackson object mapper to allow writing JSON responses (if needed).
     */
    void configure(
            final String name,
            final Collection<String> healthCheckUrlPaths,
            final HealthResponseProvider healthResponseProvider,
            final HealthEnvironment health,
            final JerseyEnvironment jersey,
            final ServletEnvironment servlets,
            final ObjectMapper mapper);
}
