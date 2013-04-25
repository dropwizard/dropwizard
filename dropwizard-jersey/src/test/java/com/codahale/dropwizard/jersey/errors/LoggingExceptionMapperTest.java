package com.codahale.dropwizard.jersey.errors;

import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.logging.LoggingFactory;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.fest.assertions.api.Fail;
import org.junit.Test;

import javax.validation.Validation;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoggingExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Path("/test/")
    @Produces(MediaType.APPLICATION_JSON)
    public static class ExampleResource {
        @GET
        public String show() throws IOException {
            throw new IOException("WHAT");
        }
    }

    @Override
    protected AppDescriptor configure() {
        final DefaultResourceConfig config = new DefaultResourceConfig();
        config.getSingletons().add(new JacksonMessageBodyProvider(Jackson.newObjectMapper(),
                                                                  Validation.buildDefaultValidatorFactory()
                                                                            .getValidator()));
        config.getSingletons().add(new LoggingExceptionMapper<Throwable>() {});
        config.getSingletons().add(new ExampleResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        try {
            client().resource("/test/").type(MediaType.APPLICATION_JSON).get(String.class);
            Fail.failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(500);

            assertThat(e.getResponse().getEntity(String.class))
                    .startsWith("{\"message\":\"There was an error processing your request. It has been logged (ID ");
        }
    }
}
