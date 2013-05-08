package com.codahale.dropwizard.testing.junit;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.Service;
import com.codahale.dropwizard.jetty.HttpConnectorFactory;
import com.codahale.dropwizard.server.DefaultServerFactory;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DropwizardServiceRuleTest {

    @ClassRule
    public static final DropwizardServiceRule<TestConfiguration> RULE =
            new DropwizardServiceRule<>(TestService.class, resourceFilePath("test-config.yaml"));

    @Test
    public void canGetExpectedResourceOverHttp() {
        final String content = new Client().resource("http://localhost:" +
                                                             RULE.getLocalPort()
                                                             +"/test").get(String.class);

        assertThat(content, is("Yes, it's here"));
    }

    @Test
    public void returnsConfiguration() {
        final TestConfiguration config = RULE.getConfiguration();
        assertThat(config.getMessage(), is("Yes, it's here"));
        final DefaultServerFactory serverFactory = (DefaultServerFactory) config.getServerFactory();
        final HttpConnectorFactory connectorFactory = (HttpConnectorFactory) serverFactory.getApplicationConnectors().get(0);
        assertThat(connectorFactory.getPort(), is(0));
    }

    @Test
    public void returnsService() {
        final TestService service = RULE.getService();
        assertNotNull(service);
    }

    @Test
    public void returnsEnvironment() {
        final Environment environment = RULE.getEnvironment();
        assertThat(environment.getName(), is("TestService"));
    }


    public static class TestService extends Service<TestConfiguration> {
        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
            environment.jersey().addResource(new TestResource(configuration.getMessage()));
        }
    }

    @Path("/")
    public static class TestResource {

        private final String message;

        public TestResource(String message) {
            this.message = message;
        }

        @Path("test")
        @GET
        public String test() {
            return message;
        }
    }

    public static class TestConfiguration extends Configuration {
        @NotEmpty
        @JsonProperty
        private String message;

        public String getMessage() {
            return message;
        }
    }

    private static String resourceFilePath(String resourceClassPathLocation) {
        try {

            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
