package io.dropwizard.testing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.PrintWriter;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class DropwizardTestSupportTest {

    public static final DropwizardTestSupport<TestConfiguration> TEST_SUPPORT =
            new DropwizardTestSupport<>(TestApplication.class, resourceFilePath("test-config.yaml"));

    @BeforeClass
    public static void setUp(){
        TEST_SUPPORT.before();
    }

    @AfterClass
    public static void tearDown(){
        TEST_SUPPORT.after();
    }

    @Test
    public void canGetExpectedResourceOverHttp() {
        final String content = ClientBuilder.newClient().target("http://localhost:" +
                TEST_SUPPORT.getLocalPort()
                +"/test")
                .request().get(String.class);

        assertThat(content, is("Yes, it's here"));
    }

    @Test
    public void returnsConfiguration() {
        final TestConfiguration config = TEST_SUPPORT.getConfiguration();
        assertThat(config.getMessage(), is("Yes, it's here"));
    }

    @Test
    public void returnsApplication() {
        final TestApplication application = TEST_SUPPORT.getApplication();
        assertNotNull(application);
    }

    @Test
    public void returnsEnvironment() {
        final Environment environment = TEST_SUPPORT.getEnvironment();
        assertThat(environment.getName(), is("TestApplication"));
    }

    @Test
    public void canPerformAdminTask() {
        final String response
                = ClientBuilder.newClient().target("http://localhost:"
                + TEST_SUPPORT.getAdminPort() + "/tasks/hello?name=test_user")
                .request()
                .post(Entity.entity("", MediaType.TEXT_PLAIN), String.class);

        assertThat(response, is("Hello has been said to test_user"));
    }

    public static class TestApplication extends Application<TestConfiguration> {
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

        @NotEmpty
        @JsonProperty
        private String extra;

        public String getMessage() {
            return message;
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