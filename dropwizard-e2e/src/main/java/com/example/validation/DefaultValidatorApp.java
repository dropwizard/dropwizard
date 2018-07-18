package com.example.validation;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

public class DefaultValidatorApp extends Application<Configuration> {

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(ValidatedResource.class);
    }
}
