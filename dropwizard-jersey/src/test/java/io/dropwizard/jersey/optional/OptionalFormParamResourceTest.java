package io.dropwizard.jersey.optional;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.MyMessage;
import io.dropwizard.jersey.MyMessageParamConverterProvider;
import io.dropwizard.jersey.params.UUIDParam;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OptionalFormParamResourceTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(OptionalFormParamResource.class)
                .register(MyMessageParamConverterProvider.class);
    }

    @Test
    void shouldReturnDefaultMessageWhenMessageIsNotPresent() {
        final String defaultMessage = "Default Message";
        final Response response = target("/optional/message").request().post(Entity.form(new MultivaluedStringMap()));

        assertThat(response.readEntity(String.class)).isEqualTo(defaultMessage);
    }

    @Test
    void shouldReturnDefaultMessageWhenMessageBlank() {
        String defaultMessage = "Default Message";
        final Form form = new Form("message", "");
        final Response response = target("/optional/message").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class)).isEqualTo(defaultMessage);
    }

    @Test
    void shouldReturnMessageWhenMessageIsPresent() {
        final String customMessage = "Custom Message";
        final Form form = new Form("message", customMessage);
        final Response response = target("/optional/message").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class)).isEqualTo(customMessage);
    }

    @Test
    void shouldReturnDefaultMessageWhenMyMessageIsNotPresent() {
        final String defaultMessage = "My Default Message";
        final Response response = target("/optional/my-message").request().post(Entity.form(new MultivaluedStringMap()));

        assertThat(response.readEntity(String.class)).isEqualTo(defaultMessage);
    }

    @Test
    void shouldReturnMyMessageWhenMyMessageIsPresent() {
        final String myMessage = "My Message";
        final Form form = new Form("mymessage", myMessage);
        final Response response = target("/optional/my-message").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class)).isEqualTo(myMessage);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidUUIDIsPresent() {
        final String invalidUUID = "invalid-uuid";
        final Form form = new Form("uuid", invalidUUID);
        final Response response = target("/optional/uuid").request().post(Entity.form(form));

        assertThat(response.getStatus()).isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    void shouldReturnDefaultUUIDWhenUUIDIsNotPresent() {
        final String defaultUUID = "d5672fa8-326b-40f6-bf71-d9dacf44bcdc";
        final Response response = target("/optional/uuid").request().post(Entity.form(new MultivaluedStringMap()));

        assertThat(response.readEntity(String.class)).isEqualTo(defaultUUID);
    }

    @Test
    void shouldReturnUUIDWhenValidUUIDIsPresent() {
        final String uuid = "fd94b00d-bd50-46b3-b42f-905a9c9e7d78";
        final Form form = new Form("uuid", uuid);
        final Response response = target("/optional/uuid").request().post(Entity.form(form));

        assertThat(response.readEntity(String.class)).isEqualTo(uuid);
    }

    @Path("/optional")
    public static class OptionalFormParamResource {

        @POST
        @Path("/message")
        public String getMessage(@FormParam("message") Optional<String> message) {
            return message.orElse("Default Message");
        }

        @POST
        @Path("/my-message")
        public String getMyMessage(@FormParam("mymessage") Optional<MyMessage> myMessage) {
            return myMessage.orElse(new MyMessage("My Default Message")).getMessage();
        }

        @POST
        @Path("/uuid")
        public String getUUID(@FormParam("uuid") Optional<UUIDParam> uuid) {
            return uuid.orElse(new UUIDParam("d5672fa8-326b-40f6-bf71-d9dacf44bcdc")).get().toString();
        }
    }
}
