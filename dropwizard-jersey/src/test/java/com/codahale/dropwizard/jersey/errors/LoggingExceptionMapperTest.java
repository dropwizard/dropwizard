package com.codahale.dropwizard.jersey.errors;

import com.codahale.dropwizard.logging.LoggingFactory;
import com.codahale.dropwizard.util.RequestId;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import org.apache.log4j.MDC;
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

    @Test
    public void requestIdInMDC() {
      LoggingExceptionMapper mapper = new DefaultLoggingExceptionMapper();
      try {
          MDC.put(RequestId.SERVICE_REQUEST_ID, "1234");
          assertThat(mapper.getServiceRequestId()).isEqualTo(1234);
      } finally {
          MDC.remove(RequestId.SERVICE_REQUEST_ID);
      }
    }

    @Test
    public void requestIdNotInMDC() {
        LoggingExceptionMapper mapper = new DefaultLoggingExceptionMapper();
        assertThat(mapper.getServiceRequestId()).isNotNull();
    }
}
