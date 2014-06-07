package io.dropwizard.jersey.errors;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

public class LoggingExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected Application configure() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.register(new DefaultLoggingExceptionMapper());
        rc = rc.register(new DefaultJacksonMessageBodyProvider());
        rc = rc.register(ExceptionResource.class);
        return rc;
    }

    @Test
    public void returnsAnErrorMessage() throws Exception {
        try {
            target("/exception/").request(MediaType.APPLICATION_JSON).get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            Response response = e.getResponse();
            assertThat(response.getStatus())
                    .isEqualTo(500);
            
            final String responseStr = response.readEntity(String.class);

            assertThat(responseStr)
                    .startsWith("{\"message\":\"There was an error processing your request. It has been logged (ID ");
        }
    }
}
