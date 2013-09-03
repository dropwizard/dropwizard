package com.codahale.dropwizard.testing.junit;

import com.codahale.dropwizard.server.ServerApplication;
import com.codahale.dropwizard.server.ServerEnvironment;
import com.codahale.dropwizard.setup.Bootstrap;

public class TestApplication extends ServerApplication<TestConfiguration> {

    @Override
    public void initializeServer(Bootstrap<TestConfiguration> bootstrap) {
    }

    @Override
    public void run(TestConfiguration configuration, ServerEnvironment environment) throws Exception {
        environment.jersey().register(new TestResource(configuration.getMessage()));
    }
}
