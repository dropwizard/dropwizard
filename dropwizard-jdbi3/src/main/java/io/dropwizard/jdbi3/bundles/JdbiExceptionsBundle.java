package io.dropwizard.jdbi3.bundles;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jdbi3.jersey.LoggingJdbiExceptionMapper;
import io.dropwizard.jdbi3.jersey.LoggingSQLExceptionMapper;

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
