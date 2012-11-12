package com.yammer.dropwizard.jdbi.bundles;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jdbi.jersey.LoggingDBIExceptionMapper;
import com.yammer.dropwizard.jdbi.jersey.LoggingSQLExceptionMapper;

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
        environment.addProvider(new LoggingSQLExceptionMapper());
        environment.addProvider(new LoggingDBIExceptionMapper());
    }
}
