package io.dropwizard.jdbi3.bundles;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.jdbi3.jersey.LoggingJdbiExceptionMapper;
import io.dropwizard.jdbi3.jersey.LoggingSQLExceptionMapper;
import io.dropwizard.setup.Environment;

/**
 * A bundle for logging {@link java.sql.SQLException}s and {@link org.jdbi.v3.core.JdbiException}s
 * so that their actual causes aren't overlooked.
 */
public class JdbiExceptionsBundle implements ConfiguredBundle<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) {
        environment.jersey().register(new LoggingSQLExceptionMapper());
        environment.jersey().register(new LoggingJdbiExceptionMapper());
    }
}
