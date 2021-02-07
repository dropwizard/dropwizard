package io.dropwizard.jersey.errors;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.jupiter.api.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class LoggingExceptionMapperTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(DefaultLoggingExceptionMapper.class)
                .register(DefaultJacksonMessageBodyProvider.class)
                .register(ExceptionResource.class);
    }

    @Test
    void returnsAnErrorMessage() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> target("/exception/").request(MediaType.APPLICATION_JSON).get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(500))
            .satisfies(e -> assertThat(e.getResponse().readEntity(String.class)).startsWith("{\"code\":500,\"message\":"
                + "\"There was an error processing your request. It has been logged (ID "));
    }

    @Test
    void handlesJsonMappingException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> target("/exception/json-mapping-exception").request(MediaType.APPLICATION_JSON).get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(500))
            .satisfies(e -> assertThat(e.getResponse().readEntity(String.class)).startsWith("{\"code\":500,\"message\":"
                + "\"There was an error processing your request. It has been logged (ID "));
    }

    @Test
    void handlesMethodNotAllowedWithHeaders() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> target("/exception/json-mapping-exception")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.json("A"), String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(405))
            .satisfies(e -> assertThat(e.getResponse().getAllowedMethods()).containsOnly("GET", "OPTIONS"))
            .satisfies(e -> assertThat(e.getResponse().readEntity(String.class))
                .isEqualTo("{\"code\":405,\"message\":\"HTTP 405 Method Not Allowed\"}"));
    }

    @Test
    void formatsWebApplicationException() {
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> target("/exception/web-application-exception").request(MediaType.APPLICATION_JSON).get(String.class))
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
