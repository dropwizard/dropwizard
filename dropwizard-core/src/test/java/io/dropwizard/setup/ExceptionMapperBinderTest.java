package io.dropwizard.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jersey.validation.JerseyViolationException;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.util.Resources;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validator;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.File;

import static io.dropwizard.server.SimpleServerFactoryTest.httpRequest;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionMapperBinderTest {
    private SimpleServerFactory http;
    private Environment environment = new Environment("testEnvironment");

    @BeforeEach
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = environment.getObjectMapper();
        final Validator validator = environment.getValidator();
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
