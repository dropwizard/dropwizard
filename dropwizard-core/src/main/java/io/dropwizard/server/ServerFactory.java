package io.dropwizard.server;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;

/**
 * A factory for building {@link Server} instances for Dropwizard applications.
 *
 * @see DefaultServerFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultServerFactory.class)
public interface ServerFactory extends Discoverable {
    /**
     * Build a server for the given Dropwizard application.
     *
     * @param environment the application's environment
     * @return a {@link Server} running the Dropwizard application
     */
    Server build(Environment environment);

    /**
     * Configures the given environment with settings defined in the factory.
     *
     * @param environment the application's environment
     */
    void configure(Environment environment);
}
