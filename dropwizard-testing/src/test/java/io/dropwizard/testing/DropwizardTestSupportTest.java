package io.dropwizard.testing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.servlets.tasks.PostBodyTask;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.BaseValidator;

import javax.validation.constraints.NotEmpty;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.Validator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardTestSupportTest {

    public static final DropwizardTestSupport<TestConfiguration> TEST_SUPPORT =
            new DropwizardTestSupport<>(TestApplication.class, resourceFilePath("test-config.yaml"));

    @BeforeClass
    public static void setUp() {
        TEST_SUPPORT.before();
    }

    @AfterClass
    public static void tearDown() {
        TEST_SUPPORT.after();
    }

    @Test
    public void canGetExpectedResourceOverHttp() {
        final String content = ClientBuilder.newClient().target(
            "http://localhost:" + TEST_SUPPORT.getLocalPort() + "/test").request().get(String.class);

        assertThat(content).isEqualTo("Yes, it's here");
    }

    @Test
    public void returnsConfiguration() {
        final TestConfiguration config = TEST_SUPPORT.getConfiguration();
        assertThat(config.getMessage()).isEqualTo("Yes, it's here");
    }

    @Test
    public void returnsApplication() {
        final TestApplication application = TEST_SUPPORT.getApplication();
        assertThat(application).isNotNull();
    }

    @Test
    public void returnsEnvironment() {
        final Environment environment = TEST_SUPPORT.getEnvironment();
        assertThat(environment.getName()).isEqualTo("TestApplication");
    }

    @Test
    public void canPerformAdminTask() {
        final String response
                = ClientBuilder.newClient().target("http://localhost:"
                + TEST_SUPPORT.getAdminPort() + "/tasks/hello?name=test_user")
                .request()
                .post(Entity.entity("", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Hello has been said to test_user");
    }

    @Test
    public void canPerformAdminTaskWithPostBody() {
        final String response
            = ClientBuilder.newClient().target("http://localhost:"
            + TEST_SUPPORT.getAdminPort() + "/tasks/echo")
            .request()
            .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }
    
    @Test
    public void isCustomFactoryCalled() throws IOException, ConfigurationException {
        //load the test-config so that we can call the support with an explicit config
        TestConfiguration config = new YamlConfigurationFactory<>(
            TestConfiguration.class, 
            BaseValidator.newValidator(),
            Jackson.newObjectMapper(),
            "dw"
        ).build(new File(resourceFilePath("test-config.yaml")));
        
        DropwizardTestSupport<TestConfiguration> support = new DropwizardTestSupport<>(
            FailingApplication.class, 
            config
        );
        try {
            support.before();
        } finally {
            support.after();
        }
    }

    public static class FailingApplication extends Application<TestConfiguration> {
        
        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.setConfigurationFactoryFactory(FailingConfigurationFactory::new);
        }
        
        @Override
        public void run(TestConfiguration configuration, Environment environment) {}
    }
    
    public static class FailingConfigurationFactory extends JsonConfigurationFactory<TestConfiguration> {

        public FailingConfigurationFactory(Class<TestConfiguration> klass, Validator validator, ObjectMapper objectMapper, String propertyPrefix) {
            super(klass, validator, objectMapper, propertyPrefix);
            throw new IllegalStateException();
        }
        
    }

    public static class TestApplication extends Application<TestConfiguration> {
        @Override
        public void run(TestConfiguration configuration, Environment environment) {
            environment.jersey().register(new TestResource(configuration.getMessage()));
            environment.admin().addTask(new HelloTask());
            environment.admin().addTask(new EchoTask());
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
        private String message = "";

        @NotEmpty
        @JsonProperty
        private String extra = "";

        public String getMessage() {
            return message;
        }
    }

    public static class HelloTask extends Task {

        public HelloTask() {
            super("hello");
        }

        @Override
        public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
            List<String> names = parameters.getOrDefault("name", Collections.emptyList());
            String name = !names.isEmpty() ? names.get(0) : "Anonymous";
            output.print("Hello has been said to " + name);
            output.flush();
        }
    }

    public static class EchoTask extends PostBodyTask {

        public EchoTask() {
            super("echo");
        }

        @Override
        public void execute(Map<String, List<String>> parameters, String body, PrintWriter output) throws Exception {
            output.print(body);
            output.flush();
        }
    }
}
