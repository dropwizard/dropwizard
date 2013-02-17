package com.yammer.dropwizard.testing.tests.junit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.testing.junit.DropwizardServiceRule;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.File;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DropwizardServiceRuleTest {

    @ClassRule
    public static DropwizardServiceRule dropwizardServiceRule = new DropwizardServiceRule(
            TestService.class,
            TestConfiguration.class,
            resourceFilePath("test-config.yaml"));

    @Test
    public void canGetExpectedResourceOverHttp() {
        String content = new Client().resource("http://localhost:8080/test").get(String.class);

        assertThat(content, is("Yes, it's here"));
    }




    public static class TestService extends Service<TestConfiguration> {
        @Override
        public void initialize(Bootstrap bootstrap) {
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
            environment.addResource(new TestResource(configuration.getMessage()));
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

        @JsonProperty
        @NotEmpty
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
