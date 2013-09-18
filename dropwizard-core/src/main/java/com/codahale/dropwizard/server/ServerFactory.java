package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jackson.Discoverable;
import com.codahale.dropwizard.setup.Environment;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
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
}
