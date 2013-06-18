package com.codahale.dropwizard.jersey.jackson;

import com.codahale.dropwizard.logging.LoggingFactory;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.failBecauseExceptionWasNotThrown;

public class JsonProcessingExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder("com.codahale.dropwizard.jersey.jackson").build();
    }

    @Test
    public void returnsA500ForNonDeserializableRepresentationClasses() throws Exception {
        try {
            resource().path("/json/broken")
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
            resource().path("/json/ok")
                      .type(MediaType.APPLICATION_JSON)
                      .post("{\"bork\":100}");
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(400);
        }
    }
}
