package io.dropwizard.testing.junit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.Resources;
import com.sun.jersey.api.client.Client;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.File;
import java.io.PrintWriter;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DropwizardAppRuleTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, resourceFilePath("test-config.yaml"));

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
    }

    @Test
    public void returnsApplication() {
        final TestApplication application = RULE.getApplication();
        assertNotNull(application);
    }

    @Test
    public void returnsEnvironment() {
        final Environment environment = RULE.getEnvironment();
        assertThat(environment.getName(), is("TestApplication"));
    }

    @Test
    public void canPerformAdminTask() {
        final String response = new Client().resource("http://localhost:" +
                RULE.getAdminPort() + "/tasks/hello?name=test_user")
                .post(String.class);
        assertThat(response, is("Hello has been said to test_user"));
    }

    public static class TestApplication extends Application<TestConfiguration> {
        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
            environment.jersey().register(new TestResource(configuration.getMessage()));
            environment.admin().addTask(new HelloTask());
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

    public static String resourceFilePath(String resourceClassPathLocation) {
        try {
            return new File(Resources.getResource(resourceClassPathLocation).toURI()).getAbsolutePath();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static class HelloTask extends Task {

        public HelloTask() {
            super("hello");
        }

        @Override
        public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
            ImmutableCollection<String> names = parameters.get("name");
            String name = !names.isEmpty() ? names.asList().get(0) : "Anonymous";
            output.print("Hello has been said to " + name);
            output.flush();
        }
    }
}
