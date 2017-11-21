package io.dropwizard.testing.common;

import com.codahale.metrics.health.HealthCheck;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.DropwizardTestSupport;

import java.net.URI;

public class DropwizardClient {
    private final Object[] resources;
    private final DropwizardTestSupport<Configuration> testSupport;

    public DropwizardClient(Object... resources) {
        testSupport = new DropwizardTestSupport<Configuration>(FakeApplication.class, "") {
            @Override
            public Application<Configuration> newApplication() {
                return new FakeApplication();
            }
        };
        this.resources = resources;
    }

    public URI baseUri() {
        return URI.create("http://localhost:" + testSupport.getLocalPort() + "/application");
    }

    public ObjectMapper getObjectMapper() {
        return testSupport.getObjectMapper();
    }

    public Environment getEnvironment() {
        return testSupport.getEnvironment();
    }

    public void before() throws Throwable {
        testSupport.before();
    }

    public void after() {
        testSupport.after();
    }

    private static class DummyHealthCheck extends HealthCheck {
        @Override
        protected Result check() {
            return Result.healthy();
        }
    }

    private class FakeApplication extends Application<Configuration> {
        @Override
        public void run(Configuration configuration, Environment environment) {
            final SimpleServerFactory serverConfig = new SimpleServerFactory();
            configuration.setServerFactory(serverConfig);
            final HttpConnectorFactory connectorConfig = (HttpConnectorFactory) serverConfig.getConnector();
            connectorConfig.setPort(0);

            environment.healthChecks().register("dummy", new DummyHealthCheck());

            for (Object resource : resources) {
                if (resource instanceof Class<?>) {
                    environment.jersey().register((Class<?>) resource);
                } else {
                    environment.jersey().register(resource);
                }
            }
        }
    }
}
