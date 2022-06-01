package io.dropwizard.jersey.params;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import org.junit.jupiter.api.Test;

class NonEmptyStringParamProviderTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting().register(NonEmptyStringParamResource.class);
    }

    @Test
    void shouldReturnDefaultMessageWhenNonExistent() {
        String response = target("/non-empty/string").request().get(String.class);
        assertThat(response).isEqualTo("Hello");
    }

    @Test
    void shouldReturnDefaultMessageWhenEmptyString() {
        String response =
                target("/non-empty/string").queryParam("message", "").request().get(String.class);
        assertThat(response).isEqualTo("Hello");
    }

    @Test
    void shouldReturnDefaultMessageWhenNull() {
        String response =
                target("/non-empty/string").queryParam("message").request().get(String.class);
        assertThat(response).isEqualTo("Hello");
    }

    @Test
    void shouldReturnMessageWhenSpecified() {
        String response = target("/non-empty/string")
                .queryParam("message", "Goodbye")
                .request()
                .get(String.class);
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
