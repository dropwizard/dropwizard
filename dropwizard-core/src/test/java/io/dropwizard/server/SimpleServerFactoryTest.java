package io.dropwizard.server;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.setup.Environment;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class SimpleServerFactoryTest {

    private SimpleServerFactory http;
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private Validator validator = BaseValidator.newValidator();
    private Environment environment = new Environment("testEnvironment", objectMapper, validator, new MetricRegistry(),
            ClassLoader.getSystemClassLoader());

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                FileAppenderFactory.class, SyslogAppenderFactory.class, HttpConnectorFactory.class);
        http = new ConfigurationFactory<>(SimpleServerFactory.class, validator, objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/simple_server.yml").toURI()));
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(SimpleServerFactory.class);
    }

    @Test
    public void testGetAdminContext() {
        assertThat(http.getAdminContextPath()).isEqualTo("/secret");
    }

    @Test
    public void testGetApplicationContext() {
        assertThat(http.getApplicationContextPath()).isEqualTo("/service");
    }

    @Test
    public void testGetPort() {
        final HttpConnectorFactory connector = (HttpConnectorFactory) http.getConnector();
        assertThat(connector.getPort()).isEqualTo(0);
    }

    @Test
    public void testBuild() throws Exception {
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
    public void testConfiguredEnvironment() {
        http.configure(environment);

    	assertEquals(http.getAdminContextPath(), environment.getAdminContext().getContextPath());
    	assertEquals(http.getApplicationContextPath(), environment.getApplicationContext().getContextPath());
    }

    private static String httpRequest(String requestMethod, String url) throws Exception {
        final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(requestMethod);
        connection.connect();
        try (InputStream inputStream = connection.getInputStream()) {
            return CharStreams.toString(new InputStreamReader(inputStream));
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
        public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
            final String name = parameters.get("name").iterator().next();
            output.print("Hello, " + name + "!");
            output.flush();
        }
    }
}
