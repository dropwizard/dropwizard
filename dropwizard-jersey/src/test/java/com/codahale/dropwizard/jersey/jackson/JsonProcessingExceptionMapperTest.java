package com.codahale.dropwizard.jersey.jackson;

import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.logging.LoggingFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.junit.Test;

import javax.validation.Validation;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.failBecauseExceptionWasNotThrown;

public class JsonProcessingExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    public static class BrokenRepresentation {
        private List<String> messages;

        public BrokenRepresentation(List<String> messages) {
            this.messages = messages;
        }

        @JsonProperty
        public List<String> getMessages() {
            return messages;
        }

        @JsonProperty
        public void setMessages(List<String> messages) {
            this.messages = messages;
        }
    }

    public static class OkRepresentation {
        private String message;

        @JsonProperty
        public String getMessage() {
            return message;
        }

        @JsonProperty
        public void setMessage(String message) {
            this.message = message;
        }
    }

    @Path("/test/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public static class ExampleResource {
        @POST
        @Path("/broken")
        public void broken(BrokenRepresentation rep) throws IOException {
            System.out.println(rep);
        }

        @POST
        @Path("/ok")
        public List<String> ok(OkRepresentation rep) throws IOException {
            return ImmutableList.of(rep.getMessage());
        }
    }

    @Override
    protected AppDescriptor configure() {
        final DefaultResourceConfig config = new DefaultResourceConfig();
        config.getSingletons().add(new JacksonMessageBodyProvider(Jackson.newObjectMapper(),
                                                                  Validation.buildDefaultValidatorFactory()
                                                                            .getValidator()));
        config.getSingletons().add(new JsonProcessingExceptionMapper());
        config.getSingletons().add(new ExampleResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void returnsA500ForNonDeserializableRepresentationClasses() throws Exception {
        try {
            client().resource("/test/broken")
                    .type(MediaType.APPLICATION_JSON)
                    .post(new BrokenRepresentation(ImmutableList.of("whee")));
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(500);
        }
    }

    @Test
    public void returnsA400ForNonDeserializableRequestEntities() throws Exception {
        try {
            client().resource("/test/ok")
                    .type(MediaType.APPLICATION_JSON)
                    .post("{\"bork\":100}");
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(400);
        }
    }
}
