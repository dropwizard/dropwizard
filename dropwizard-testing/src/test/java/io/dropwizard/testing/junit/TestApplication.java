package io.dropwizard.testing.junit;

import io.dropwizard.HttpApplication;
import io.dropwizard.setup.HttpEnvironment;

public class TestApplication extends HttpApplication<TestConfiguration> {
    @Override
    public void run(TestConfiguration configuration, HttpEnvironment environment) throws Exception {
        environment.jersey().register(new TestResource(configuration.getMessage()));
    }
}
