package com.yammer.dropwizard.bundles;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.LoggingDBIExceptionMapper;
import com.yammer.dropwizard.jersey.LoggingSQLExceptionMapper;

/**
 * A bundle for logging SQLExceptions and DBIExceptions so that their actual causes aren't overlooked.
 */
public class DBIExceptionsBundle implements Bundle {
    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new LoggingSQLExceptionMapper());
        environment.addProvider(new LoggingDBIExceptionMapper());
    }
}
