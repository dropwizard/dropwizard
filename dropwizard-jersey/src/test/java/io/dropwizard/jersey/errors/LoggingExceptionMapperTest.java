package io.dropwizard.jersey.errors;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class LoggingExceptionMapperTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
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
    public void handlesMethodNotAllowedWithHeaders() {
        final Throwable thrown = catchThrowable(() -> target("/exception/json-mapping-exception")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json("A"), String.class));
        assertThat(thrown).isInstanceOf(WebApplicationException.class);
        final Response resp = ((WebApplicationException) thrown).getResponse();
        assertThat(resp.getStatus()).isEqualTo(405);
        assertThat(resp.getHeaders()).contains(entry("Allow", Collections.singletonList("GET,OPTIONS")));
        assertThat(resp.readEntity(String.class)).isEqualTo("{\"code\":405,\"message\":\"HTTP 405 Method Not Allowed\"}");
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
