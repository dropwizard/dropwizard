package com.codahale.dropwizard.jdbi.bundles;

import com.codahale.dropwizard.jdbi.jersey.LoggingDBIExceptionMapper;
import com.codahale.dropwizard.jdbi.jersey.LoggingSQLExceptionMapper;
import com.codahale.dropwizard.server.ServerBundle;
import com.codahale.dropwizard.server.ServerEnvironment;
import com.codahale.dropwizard.setup.Bootstrap;

/**
 * A bundle for logging SQLExceptions and DBIExceptions so that their actual causes aren't overlooked.
 */
public class DBIExceptionsBundle extends ServerBundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // nothing doing
    }

    @Override
    public void run(ServerEnvironment environment) {
        environment.jersey().register(new LoggingSQLExceptionMapper());
        environment.jersey().register(new LoggingDBIExceptionMapper());
    }
}
