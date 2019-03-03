package io.dropwizard.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.JsonConfigurationFactory;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.servlets.tasks.PostBodyTask;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.TestConfiguration;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.validation.Validator;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardTestSupportTest {
    private static final TestServiceListener<TestConfiguration> TEST_SERVICE_LISTENER = new TestServiceListener<>();
    private static final TestManaged TEST_MANAGED = new TestManaged();
    private static final DropwizardTestSupport<TestConfiguration> TEST_SUPPORT =
            new DropwizardTestSupport<>(TestApplication.class, resourceFilePath("test-config.yaml"))
                    .addListener(TEST_SERVICE_LISTENER)
                    .manage(TEST_MANAGED);

    @BeforeAll
    static void setUp() throws Exception {
        assertThat(TEST_SERVICE_LISTENER.executedOnRun).isFalse();
        assertThat(TEST_MANAGED.executedStart).isFalse();
        TEST_SUPPORT.before();
        assertThat(TEST_SERVICE_LISTENER.executedOnRun).isTrue();
        assertThat(TEST_MANAGED.executedStart).isTrue();
    }

    @AfterAll
    static void tearDown() {
        assertThat(TEST_SERVICE_LISTENER.executedOnStop).isFalse();
        assertThat(TEST_MANAGED.executedStop).isFalse();
        TEST_SUPPORT.after();
        assertThat(TEST_SERVICE_LISTENER.executedOnStop).isTrue();
        assertThat(TEST_MANAGED.executedStop).isTrue();
    }

    @Test
    void canGetExpectedResourceOverHttp() {
        final String content = ClientBuilder.newClient().target(
                "http://localhost:" + TEST_SUPPORT.getLocalPort() + "/test").request().get(String.class);

        assertThat(content).isEqualTo("Yes, it's here");
    }

    @Test
    void returnsConfiguration() {
        final TestConfiguration config = TEST_SUPPORT.getConfiguration();
        assertThat(config.getMessage()).isEqualTo("Yes, it's here");
    }

    @Test
    void returnsApplication() {
        final TestApplication application = TEST_SUPPORT.getApplication();
        assertThat(application).isNotNull();
    }

    @Test
    void returnsEnvironment() {
        final Environment environment = TEST_SUPPORT.getEnvironment();
        assertThat(environment.getName()).isEqualTo("TestApplication");
    }

    @Test
    void canPerformAdminTask() {
        final String response
                = ClientBuilder.newClient().target("http://localhost:"
                + TEST_SUPPORT.getAdminPort() + "/tasks/hello?name=test_user")
                .request()
                .post(Entity.entity("", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Hello has been said to test_user");
    }

    @Test
    void canPerformAdminTaskWithPostBody() {
        final String response
                = ClientBuilder.newClient().target("http://localhost:"
                + TEST_SUPPORT.getAdminPort() + "/tasks/echo")
                .request()
                .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }

    @Test
    void isCustomFactoryCalled() throws Exception {
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
        public void run(TestConfiguration configuration, Environment environment) {
        }
    }

    public static class FailingConfigurationFactory extends JsonConfigurationFactory<TestConfiguration> {
        FailingConfigurationFactory(Class<TestConfiguration> klass, Validator validator, ObjectMapper objectMapper, String propertyPrefix) {
            super(klass, validator, objectMapper, propertyPrefix);
            throw new IllegalStateException();
        }

    }

    public static class TestApplication extends io.dropwizard.testing.app.TestApplication {
        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
            super.run(configuration, environment);
            environment.admin().addTask(new HelloTask());
            environment.admin().addTask(new EchoTask());
        }
    }

    public static class HelloTask extends Task {
        HelloTask() {
            super("hello");
        }

        @Override
        public void execute(Map<String, List<String>> parameters, PrintWriter output) {
            List<String> names = parameters.getOrDefault("name", Collections.emptyList());
            String name = !names.isEmpty() ? names.get(0) : "Anonymous";
            output.print("Hello has been said to " + name);
            output.flush();
        }
    }

    public static class EchoTask extends PostBodyTask {
        EchoTask() {
            super("echo");
        }

        @Override
        public void execute(Map<String, List<String>> parameters, String body, PrintWriter output) {
            output.print(body);
            output.flush();
        }
    }

    public static class TestServiceListener<T extends Configuration> extends DropwizardTestSupport.ServiceListener<T> {
        volatile boolean executedOnRun = false;
        volatile boolean executedOnStop = false;

        @Override
        public void onRun(T configuration, Environment environment, DropwizardTestSupport<T> rule) throws Exception {
            super.onRun(configuration, environment, rule);
            executedOnRun = true;
        }

        @Override
        public void onStop(DropwizardTestSupport<T> rule) throws Exception {
            super.onStop(rule);
            executedOnStop = true;
        }
    }

    public static class TestManaged implements Managed {
        volatile boolean executedStart = false;
        volatile boolean executedStop = false;

        @Override
        public void start() {
            executedStart = true;
        }

        @Override
        public void stop() {
            executedStop = true;
        }
    }
}
