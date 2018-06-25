package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.JerseyViolationException;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Server;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.File;

import static io.dropwizard.server.SimpleServerFactoryTest.httpRequest;
import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionMapperBinderTest {
    private SimpleServerFactory http;
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private Validator validator = BaseValidator.newValidator();
    private Environment environment = new Environment("testEnvironment", objectMapper, validator, new MetricRegistry(),
        ClassLoader.getSystemClassLoader());

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
            FileAppenderFactory.class, SyslogAppenderFactory.class, HttpConnectorFactory.class);
        http = (SimpleServerFactory) new YamlConfigurationFactory<>(ServerFactory.class, validator, objectMapper, "dw")
            .build(new File(Resources.getResource("yaml/simple_server.yml").toURI()));
    }

    @Test
    public void testOverrideDefaultExceptionMapper() throws Exception {
        environment.jersey().register(new TestValidationResource());
        environment.jersey().register(new MyJerseyExceptionMapper());
        final Server server = http.build(environment);
        server.start();

        final int port = ((AbstractNetworkConnector) server.getConnectors()[0]).getLocalPort();
        assertThat(httpRequest("GET", "http://localhost:" + port + "/service/test")).isEqualTo("alright!");
        server.stop();
    }

    private static class MyJerseyExceptionMapper implements ExceptionMapper<JerseyViolationException> {
        @Override
        public Response toResponse(JerseyViolationException e) {
            return Response.ok("alright!").build();
        }
    }

    @Path("/test")
    @Produces("application/json")
    private static class TestValidationResource {
        @GET
        public String get(@NotEmpty @QueryParam("foo") String foo) {
            return foo;
        }
    }
}
