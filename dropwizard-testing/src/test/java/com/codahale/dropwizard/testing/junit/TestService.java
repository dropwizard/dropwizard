package com.codahale.dropwizard.testing.junit;

import com.codahale.dropwizard.Service;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;

public class TestService extends Service<TestConfiguration> {

    @Override
    public void initialize(Bootstrap<TestConfiguration> bootstrap) {
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().addResource(new TestResource(configuration.getMessage()));
    }
}
