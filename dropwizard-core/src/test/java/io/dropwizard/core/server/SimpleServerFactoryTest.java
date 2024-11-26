package io.dropwizard.core.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.FileAppenderFactory;
import io.dropwizard.logging.common.SyslogAppenderFactory;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.Validator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SimpleServerFactoryTest {

    private SimpleServerFactory http;
    private final Environment environment = new Environment("testEnvironment");

    @BeforeEach
    void setUp() throws Exception {
        final ObjectMapper objectMapper = environment.getObjectMapper();
        final Validator validator = environment.getValidator();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                FileAppenderFactory.class, SyslogAppenderFactory.class, HttpConnectorFactory.class);
        http = (SimpleServerFactory) new YamlConfigurationFactory<>(ServerFactory.class, validator, objectMapper, "dw")
                .build(new ResourceConfigurationSourceProvider(), "yaml/simple_server.yml");
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(SimpleServerFactory.class);
    }

    @Test
    void testGetAdminContext() {
        assertThat(http.getAdminContextPath()).isEqualTo("/secret");
    }

    @Test
    void testGetApplicationContext() {
        assertThat(http.getApplicationContextPath()).isEqualTo("/service");
    }

    @Test
    void testGetPort() {
        assertThat(http.getConnector())
            .isInstanceOfSatisfying(HttpConnectorFactory.class, httpConnectorFactory ->
                assertThat(httpConnectorFactory.getPort()).isZero());
    }

    @Test
    void testBuild() throws Exception {
        environment.jersey().register(new TestResource());
        environment.admin().addTask(new TestTask());

        final Server server = http.build(environment);
        server.start();

        final int port = ((AbstractNetworkConnector) server.getConnectors()[0]).getLocalPort();
        assertThat(httpRequest("GET", "http://localhost:" + port + "/service/test"))
                .isEqualTo("{\"hello\": \"World\"}");
        assertThat(httpRequest("POST", "http://localhost:" + port + "/secret/tasks/hello?name=test_user"))
                .isEqualTo("Hello, test_user!");

        server.stop();
    }

    @Test
    void testConfiguredEnvironment() {
        http.configure(environment);

        assertThat(environment.getAdminContext().getContextPath()).isEqualTo(http.getAdminContextPath());
        assertThat(environment.getApplicationContext().getContextPath()).isEqualTo(http.getApplicationContextPath());
    }

    @Test
    void testDeserializeWithoutJsonAutoDetect() throws ConfigurationException, IOException {
        final ObjectMapper objectMapper = Jackson.newObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);

        assertThat(new YamlConfigurationFactory<>(
                SimpleServerFactory.class,
                BaseValidator.newValidator(),
                objectMapper,
                "dw"
                ).build(new ResourceConfigurationSourceProvider(), "yaml/simple_server.yml")
                .getApplicationContextPath())
            .isEqualTo("/service");
    }

    public static String httpRequest(String requestMethod, String url) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(requestMethod);
        connection.connect();
        try (InputStream in = connection.getInputStream()) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    @Path("/test")
    @Produces("application/json")
    public static class TestResource {

        @GET
        public String get() throws Exception {
            return "{\"hello\": \"World\"}";
        }
    }

    public static class TestTask extends Task {

        public TestTask() {
            super("hello");
        }

        @Override
        public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
            final String name = parameters.getOrDefault("name", Collections.emptyList()).iterator().next();
            output.print("Hello, " + name + "!");
            output.flush();
        }
    }
}
