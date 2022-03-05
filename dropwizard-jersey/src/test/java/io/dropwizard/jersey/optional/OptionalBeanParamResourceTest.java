package io.dropwizard.jersey.optional;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.MyMessageParamConverterProvider;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalBeanParamResourceTest extends AbstractJerseyTest {
    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(OptionalBeanParamResource.class)
                .register(MyMessageParamConverterProvider.class);
    }

    @Test
    void shouldReturnValuesIfProvidedAndValid() {
        Map<String, Object> response = target("/optional")
                .path("param")
                .queryParam("double", 1234567890D)
                .queryParam("int", 12345)
                .queryParam("long", Long.MAX_VALUE)
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("X-String", "Custom string")
                .cookie("uuid", "fd94b00d-bd50-46b3-b42f-905a9c9e7d78")
                .get(new GenericType<Map<String, Object>>() {
                });
        assertThat(response)
                .containsEntry("path", "optional/param")
                .containsEntry("pathParam", "param")
                .containsEntry("double", 1234567890D)
                .containsEntry("int", 12345)
                .containsEntry("long", Long.MAX_VALUE)
                .containsEntry("string", "Custom string")
                .containsEntry("uuid", "fd94b00d-bd50-46b3-b42f-905a9c9e7d78");
    }

    @Test
    void shouldReturnNotFoundIfValuesInvalid() {
        Response response = target("/optional")
                .path("param")
                .queryParam("double", "invalid-double")
                .queryParam("int", "invalid-int")
                .queryParam("long", "invalid-long")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .header("X-String", null)
                .cookie("uuid", "invalid-uuid")
                .get();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void shouldReturnDefaultsIfValuesMissing() {
        Map<String, Object> response = target("/optional")
                .path("param")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .get(new GenericType<Map<String, Object>>() {
                });
        assertThat(response)
                .containsEntry("path", "optional/param")
                .containsEntry("pathParam", "param")
                .containsEntry("double", Double.toString(Double.NaN))
                .containsEntry("int", 42)
                .containsEntry("long", 23)
                .containsEntry("string", "Default Message")
                .containsEntry("uuid", "d5672fa8-326b-40f6-bf71-d9dacf44bcdc");
    }

    @SuppressWarnings("NullAway")
    public static class MyBean {
        @Context
        public UriInfo uriInfo;

        @PathParam("param")
        public String pathParam;

        @QueryParam("double")
        public OptionalDouble optionalDouble;

        @QueryParam("int")
        public OptionalInt optionalInt;

        @QueryParam("long")
        public OptionalLong optionalLong;

        @HeaderParam("X-String")
        public Optional<String> string;

        @CookieParam("uuid")
        public Optional<UUID> uuid;
    }

    @Path("/optional")
    @Produces(MediaType.APPLICATION_JSON)
    public static class OptionalBeanParamResource {
        @Path("{param}")
        @GET
        public Map<String, Object> getMessage(@BeanParam MyBean bean) {
            final HashMap<String, Object> response = new HashMap<>();
            response.put("path", bean.uriInfo.getPath());
            response.put("pathParam", bean.pathParam);
            response.put("double", bean.optionalDouble.orElse(Double.NaN));
            response.put("int", bean.optionalInt.orElse(42));
            response.put("long", bean.optionalLong.orElse(23L));
            response.put("string", bean.string.orElse("Default Message"));
            response.put("uuid", bean.uuid.orElse(UUID.fromString("d5672fa8-326b-40f6-bf71-d9dacf44bcdc")));

            return response;
        }
    }
}
