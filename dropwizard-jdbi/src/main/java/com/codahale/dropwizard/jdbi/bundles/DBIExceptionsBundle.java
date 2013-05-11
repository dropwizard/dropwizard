package com.codahale.dropwizard.jdbi.bundles;

import com.codahale.dropwizard.Bundle;
import com.codahale.dropwizard.jdbi.jersey.LoggingDBIExceptionMapper;
import com.codahale.dropwizard.jdbi.jersey.LoggingSQLExceptionMapper;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;

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
        environment.jersey().addProvider(new LoggingSQLExceptionMapper());
        environment.jersey().addProvider(new LoggingDBIExceptionMapper());
    }
}
