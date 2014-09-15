package io.dropwizard.jersey.errors;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class LoggingExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .register(DefaultLoggingExceptionMapper.class)
                .register(DefaultJacksonMessageBodyProvider.class)
                .register(ExceptionResource.class);
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        try {
            target("/exception/").request(MediaType.APPLICATION_JSON).get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.readEntity(String.class)).startsWith("{\"code\":500,\"message\":"
                    + "\"There was an error processing your request. It has been logged (ID ");
        }
    }
}
