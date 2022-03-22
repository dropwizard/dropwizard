package io.dropwizard.documentation;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class CustomCommandApp extends Application<Configuration> {
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addCommand(new MyCommand());
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
    }
}
