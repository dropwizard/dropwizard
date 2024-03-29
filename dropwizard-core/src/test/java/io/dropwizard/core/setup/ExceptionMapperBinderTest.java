package io.dropwizard.core.setup;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.core.server.ServerFactory;
import io.dropwizard.core.server.SimpleServerFactory;
import io.dropwizard.jersey.validation.JerseyViolationException;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.FileAppenderFactory;
import io.dropwizard.logging.common.SyslogAppenderFactory;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.dropwizard.core.server.SimpleServerFactoryTest.httpRequest;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionMapperBinderTest {
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
    void testOverrideDefaultExceptionMapper() throws Exception {
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
