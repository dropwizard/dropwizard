package io.dropwizard.jdbi.bundles;

import io.dropwizard.Bundle;
import io.dropwizard.jdbi.jersey.LoggingDBIExceptionMapper;
import io.dropwizard.jdbi.jersey.LoggingSQLExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * A bundle for logging SQLExceptions and DBIExceptions so that their actual causes aren't overlooked.
 */
public class DBIExceptionsBundle implements Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // nothing doing
    }

    @Override
    public void run(Environment environment) {
        environment.jersey().register(new LoggingSQLExceptionMapper());
        environment.jersey().register(new LoggingDBIExceptionMapper());
    }
}
