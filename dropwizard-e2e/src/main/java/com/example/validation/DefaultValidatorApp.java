package com.example.validation;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;

public class DefaultValidatorApp extends Application<Configuration> {

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(ValidatedResource.class);
    }
}
