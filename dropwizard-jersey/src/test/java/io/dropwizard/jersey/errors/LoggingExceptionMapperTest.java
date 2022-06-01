package io.dropwizard.jersey.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

class LoggingExceptionMapperTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(DefaultLoggingExceptionMapper.class)
                .register(DefaultJacksonMessageBodyProvider.class)
                .register(ExceptionResource.class);
    }

    @Test
    void returnsAnErrorMessage() {
        Invocation.Builder request = target("/exception/").request(MediaType.APPLICATION_JSON);
        assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> request.get(String.class))
                .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(500))
                .satisfies(e -> assertThat(e.getResponse().readEntity(String.class))
                        .startsWith("{\"code\":500,\"message\":"
                                + "\"There was an error processing your request. It has been logged (ID "));
    }

    @Test
    void handlesJsonMappingException() {
        Invocation.Builder request = target("/exception/json-mapping-exception").request(MediaType.APPLICATION_JSON);
        assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> request.get(String.class))
                .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(500))
                .satisfies(e -> assertThat(e.getResponse().readEntity(String.class))
                        .startsWith("{\"code\":500,\"message\":"
                                + "\"There was an error processing your request. It has been logged (ID "));
    }

    @Test
    void handlesMethodNotAllowedWithHeaders() {
        Invocation.Builder request = target("/exception/json-mapping-exception").request(MediaType.APPLICATION_JSON);
        Entity<String> jsonEntity = Entity.json("A");
        assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> request.post(jsonEntity, String.class))
                .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(405))
                .satisfies(e -> assertThat(e.getResponse().getAllowedMethods()).containsOnly("GET", "OPTIONS"))
                .satisfies(e -> assertThat(e.getResponse().readEntity(String.class))
                        .isEqualTo("{\"code\":405,\"message\":\"HTTP 405 Method Not Allowed\"}"));
    }

    @Test
    void formatsWebApplicationException() {
        Invocation.Builder request =
                target("/exception/web-application-exception").request(MediaType.APPLICATION_JSON);
        assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> request.get(String.class))
                .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(400))
                .satisfies(e -> assertThat(e.getResponse().readEntity(String.class))
                        .isEqualTo("{\"code\":400,\"message\":\"KAPOW\"}"));
    }

    @Test
    void handlesRedirectInWebApplicationException() {
        String responseText = target("/exception/web-application-exception-with-redirect")
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
        assertThat(responseText).isEqualTo("{\"status\":\"OK\"}");
    }
}
