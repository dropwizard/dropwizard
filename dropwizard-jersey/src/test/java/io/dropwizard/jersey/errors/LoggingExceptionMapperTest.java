package io.dropwizard.jersey.errors;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class LoggingExceptionMapperTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
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

    @Test
    public void handlesJsonMappingException() throws Exception {
        try {
            target("/exception/json-mapping-exception").request(MediaType.APPLICATION_JSON).get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus()).isEqualTo(500);
            assertThat(response.readEntity(String.class)).startsWith("{\"code\":500,\"message\":"
                    + "\"There was an error processing your request. It has been logged (ID ");
        }
    }

    @Test
    public void formatsWebApplicationException() throws Exception {
        try {
            target("/exception/web-application-exception").request(MediaType.APPLICATION_JSON).get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            final Response response = e.getResponse();

            assertThat(response.getStatus()).isEqualTo(400);
            assertThat(response.readEntity(String.class)).isEqualTo("{\"code\":400,\"message\":\"KAPOW\"}");
        }
    }

    @Test
    public void handlesRedirectInWebApplicationException() {
        String responseText = target("/exception/web-application-exception-with-redirect")
            .request(MediaType.APPLICATION_JSON)
            .get(String.class);
        assertThat(responseText).isEqualTo("{\"status\":\"OK\"}");
    }
}
