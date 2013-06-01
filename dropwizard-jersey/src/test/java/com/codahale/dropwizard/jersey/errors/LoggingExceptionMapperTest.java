package com.codahale.dropwizard.jersey.errors;

import com.codahale.dropwizard.logging.LoggingFactory;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.junit.Test;

import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class LoggingExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder("com.codahale.dropwizard.jersey.errors").build();
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        try {
            resource().path("/exception/").type(MediaType.APPLICATION_JSON).get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(500);

            assertThat(e.getResponse().getEntity(String.class))
                    .startsWith("{\"message\":\"There was an error processing your request. It has been logged (ID ");
        }
    }
}
