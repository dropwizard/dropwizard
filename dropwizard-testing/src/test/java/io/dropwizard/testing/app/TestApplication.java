package io.dropwizard.testing.app;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;

public class TestApplication extends Application<TestConfiguration> {
    @Override
    public void run(TestConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new TestResource(configuration.getMessage()));
    }
}
