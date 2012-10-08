package com.yammer.dropwizard.bundles;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.LoggingDBIExceptionMapper;
import com.yammer.dropwizard.jersey.LoggingSQLExceptionMapper;

/**
 * A bundle for logging SQLExceptions and DBIExceptions so that their actual causes aren't overlooked.
 */
public class DBIExceptionsBundle extends Bundle {
    @Override
    public void run(Environment environment) {
        environment.addProvider(new LoggingSQLExceptionMapper());
        environment.addProvider(new LoggingDBIExceptionMapper());
    }
}
