package io.dropwizard.jersey.params;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for empty multi-valued UUID query parameters producing a collection of null
 * ({@code dropwizard/dropwizard#3435}).
 */
class UUIDQueryParamResourceTest extends AbstractJerseyTest {

    private static final String VALID_UUID = "ec0cf621-d744-4a1c-b1d8-4b8a44b3dad7";

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(UUIDQueryParamResource.class);
    }

    @Test
    void emptyListQueryParamIsRejected() {
        final Response response = target("/uuid/list")
                .queryParam("id", "")
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void emptySetQueryParamIsRejected() {
        final Response response = target("/uuid/set")
                .queryParam("id", "")
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void missingListQueryParamReturnsEmptyCollection() {
        final Response response = target("/uuid/list").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("0: []");
    }

    @Test
    void missingSetQueryParamReturnsEmptyCollection() {
        final Response response = target("/uuid/set").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("0: []");
    }

    @Test
    void validListQueryParamIsAccepted() {
        final Response response = target("/uuid/list")
                .queryParam("id", VALID_UUID)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("1: [" + VALID_UUID + "]");
    }

    @Test
    void validSetQueryParamIsAccepted() {
        final Response response = target("/uuid/set")
                .queryParam("id", VALID_UUID)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("1: [" + VALID_UUID + "]");
    }

    @Test
    void invalidListQueryParamIsRejected() {
        final Response response = target("/uuid/list")
                .queryParam("id", "not-a-uuid")
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void emptySingleQueryParamIsRejected() {
        final Response response = target("/uuid/single")
                .queryParam("id", "")
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void missingSingleQueryParamIsNull() {
        final Response response = target("/uuid/single").request().get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("null");
    }

    @Test
    void validSingleQueryParamIsAccepted() {
        final Response response = target("/uuid/single")
                .queryParam("id", VALID_UUID)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo(VALID_UUID);
    }

    @Path("/uuid")
    public static class UUIDQueryParamResource {
        @GET
        @Path("/list")
        public String list(@QueryParam("id") List<UUID> ids) {
            return ids.size() + ": " + ids.toString();
        }

        @GET
        @Path("/set")
        public String set(@QueryParam("id") Set<UUID> ids) {
            // Sort for stable assertion output (HashSet iteration order is undefined).
            final String body = ids.stream()
                    .map(uuid -> uuid == null ? "null" : uuid.toString())
                    .sorted()
                    .collect(Collectors.joining(", ", "[", "]"));
            return ids.size() + ": " + body;
        }

        @GET
        @Path("/single")
        public String single(@QueryParam("id") UUID id) {
            return String.valueOf(id);
        }
    }
}
