package io.dropwizard.testing.junit;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class TestApplication extends Application<TestConfiguration> {

    @Override
    public void initialize(Bootstrap<TestConfiguration> bootstrap) {
    }

    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new TestResource(configuration.getMessage()));
    }
}
