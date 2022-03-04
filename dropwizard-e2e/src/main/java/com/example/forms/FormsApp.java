package com.example.forms;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class FormsApp extends Application<Configuration> {
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(new FormsResource());
    }
}
