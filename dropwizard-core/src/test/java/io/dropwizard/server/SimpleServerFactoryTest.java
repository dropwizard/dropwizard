package io.dropwizard.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.CharStreams;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleServerFactoryTest {

    private SimpleServerFactory http;
    private Environment environment = new Environment("testEnvironment");

    @BeforeEach
    void setUp() throws Exception {
        final ObjectMapper objectMapper = environment.getObjectMapper();
        final Validator validator = environment.getValidator();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                FileAppenderFactory.class, SyslogAppenderFactory.class, HttpConnectorFactory.class);
        http = (SimpleServerFactory) new YamlConfigurationFactory<>(ServerFactory.class, validator, objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/simple_server.yml").toURI()));
    }

    @Test
    void isDiscoverable() throws Exception {
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
        final HttpConnectorFactory connector = (HttpConnectorFactory) http.getConnector();
        assertThat(connector.getPort()).isEqualTo(0);
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

        assertEquals(http.getAdminContextPath(), environment.getAdminContext().getContextPath());
        assertEquals(http.getApplicationContextPath(), environment.getApplicationContext().getContextPath());
    }

    @Test
    void testDeserializeWithoutJsonAutoDetect() {
        final ObjectMapper objectMapper = Jackson.newObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);

        assertThatCode(() -> new YamlConfigurationFactory<>(
            SimpleServerFactory.class,
            BaseValidator.newValidator(),
            objectMapper,
            "dw"
            ).build(new File(Resources.getResource("yaml/simple_server.yml").toURI()))
        ).doesNotThrowAnyException();
    }

    public static String httpRequest(String requestMethod, String url) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(requestMethod);
        connection.connect();
        try (InputStream inputStream = connection.getInputStream()) {
            return CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
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
