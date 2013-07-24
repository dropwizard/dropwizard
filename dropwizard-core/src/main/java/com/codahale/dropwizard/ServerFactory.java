package com.codahale.dropwizard;

import com.codahale.dropwizard.jackson.Discoverable;
import com.codahale.dropwizard.setup.Environment;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.util.concurrent.Service;

/**
 * A factory for building {@link Service} instances for Dropwizard applications.
 *
 * @see DefaultServerFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface ServerFactory extends Discoverable {
    /**
     * Build a server for the given Dropwizard application.
     *
     * @param environment the application's environment
     * @return a {@link Service} running the Dropwizard application
     */
    Service build(Environment environment);
}