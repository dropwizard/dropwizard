package com.codahale.dropwizard.jersey.validation;

import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.logging.LoggingFactory;
import com.codahale.dropwizard.validation.Validated;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.fest.assertions.api.Fail;
import org.hibernate.validator.constraints.NotEmpty;
import org.junit.Test;

import javax.validation.Validation;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class ConstraintViolationExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    public static class ExampleRepresentation {
        @NotEmpty
        private String name;

        @JsonProperty
        public String getName() {
            return name;
        }

        @JsonProperty
        public void setName(String name) {
            this.name = name;
        }
    }

    @Path("/test/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class ExampleResource {
        @POST
        public String blah(@Validated ExampleRepresentation representation) throws IOException {
            return representation.getName();
        }
    }

    @Override
    protected AppDescriptor configure() {
        final DefaultResourceConfig config = new DefaultResourceConfig();
        config.getSingletons().add(new JacksonMessageBodyProvider(Jackson.newObjectMapper(),
                                                                  Validation.buildDefaultValidatorFactory()
                                                                            .getValidator()));
        config.getSingletons().add(new ConstraintViolationExceptionMapper());
        config.getSingletons().add(new ExampleResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        try {
            client().resource("/test/").type(MediaType.APPLICATION_JSON).post("{}");
            Fail.failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(422);

            assertThat(e.getResponse().getEntity(String.class))
                    .isEqualTo("{\"errors\":[\"name may not be empty (was null)\"]}");
        }
    }
}
