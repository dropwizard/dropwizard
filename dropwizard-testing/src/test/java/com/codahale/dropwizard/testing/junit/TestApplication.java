package com.codahale.dropwizard.testing.junit;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;

public class TestApplication extends Application<TestConfiguration> {

    @Override
    public void initialize(Bootstrap<TestConfiguration> bootstrap) {
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().addResource(new TestResource(configuration.getMessage()));
    }
}
