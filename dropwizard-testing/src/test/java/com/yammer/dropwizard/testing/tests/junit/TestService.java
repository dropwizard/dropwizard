package com.yammer.dropwizard.testing.tests.junit;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class TestService extends Service<TestConfiguration> {

    @Override
    public void initialize(Bootstrap<TestConfiguration> bootstrap) {
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.getJerseyEnvironment().addResource(new TestResource(configuration.getMessage()));
    }
}
