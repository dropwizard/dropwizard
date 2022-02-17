package io.dropwizard.jersey.params;


import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;

import static org.assertj.core.api.Assertions.assertThat;

class NonEmptyStringParamProviderTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(NonEmptyStringParamResource.class);
    }

    @Test
    void shouldReturnDefaultMessageWhenNonExistent() {
        String response = target("/non-empty/string").request().get(String.class);
        assertThat(response).isEqualTo("Hello");
    }

    @Test
    void shouldReturnDefaultMessageWhenEmptyString() {
        String response = target("/non-empty/string").queryParam("message", "").request().get(String.class);
        assertThat(response).isEqualTo("Hello");
    }

    @Test
    void shouldReturnDefaultMessageWhenNull() {
        String response = target("/non-empty/string").queryParam("message").request().get(String.class);
        assertThat(response).isEqualTo("Hello");
    }

    @Test
    void shouldReturnMessageWhenSpecified() {
        String response = target("/non-empty/string").queryParam("message", "Goodbye").request().get(String.class);
        assertThat(response).isEqualTo("Goodbye");
    }

    @Path("/non-empty")
    public static class NonEmptyStringParamResource {
        @GET
        @Path("/string")
        public String getMessage(@QueryParam("message") NonEmptyStringParam message) {
            return message.get().orElse("Hello");
        }
    }
}
