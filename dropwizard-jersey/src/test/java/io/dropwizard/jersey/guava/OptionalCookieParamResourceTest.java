package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.MyMessage;
import io.dropwizard.jersey.MyMessageParamConverterProvider;
import io.dropwizard.jersey.params.UUIDParam;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.CookieParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Application;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OptionalCookieParamResourceTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(OptionalCookieParamResource.class)
                .register(MyMessageParamConverterProvider.class);
    }

    @Test
    void shouldReturnDefaultMessageWhenMessageIsNotPresent() {
        String defaultMessage = "Default Message";
        String response = target("/optional/message").request().get(String.class);
        assertThat(response).isEqualTo(defaultMessage);
    }

    @Test
    void shouldReturnMessageWhenMessageIsBlank() {
        String response = target("/optional/message").request().cookie("message", "").get(String.class);
        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnMessageWhenMessageIsPresent() {
        String customMessage = "Custom Message";
        String response = target("/optional/message").request().cookie("message", customMessage).get(String.class);
        assertThat(response).isEqualTo(customMessage);
    }

    @Test
    void shouldReturnDefaultMessageWhenMyMessageIsNotPresent() {
        String defaultMessage = "My Default Message";
        String response = target("/optional/my-message").request().get(String.class);
        assertThat(response).isEqualTo(defaultMessage);
    }

    @Test
    void shouldReturnMyMessageWhenMyMessageIsPresent() {
        String myMessage = "My Message";
        String response = target("/optional/my-message").request().cookie("mymessage", myMessage).get(String.class);
        assertThat(response).isEqualTo(myMessage);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidUUIDIsPresent() {
        String invalidUUID = "invalid-uuid";
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() ->
            target("/optional/uuid").request().cookie("uuid", invalidUUID).get(String.class));
    }

    @Test
    void shouldReturnDefaultUUIDWhenUUIDIsNotPresent() {
        String defaultUUID = "d5672fa8-326b-40f6-bf71-d9dacf44bcdc";
        String response = target("/optional/uuid").request().get(String.class);
        assertThat(response).isEqualTo(defaultUUID);
    }

    @Test
    void shouldReturnUUIDWhenValidUUIDIsPresent() {
        String uuid = "fd94b00d-bd50-46b3-b42f-905a9c9e7d78";
        String response = target("/optional/uuid").request().cookie("uuid", uuid).get(String.class);
        assertThat(response).isEqualTo(uuid);
    }

    @Path("/optional")
    public static class OptionalCookieParamResource {
        @GET
        @Path("/message")
        public String getMessage(@CookieParam("message") Optional<String> message) {
            return message.or("Default Message");
        }

        @GET
        @Path("/my-message")
        public String getMyMessage(@CookieParam("mymessage") Optional<MyMessage> myMessage) {
            return myMessage.or(new MyMessage("My Default Message")).getMessage();
        }

        @GET
        @Path("/uuid")
        public String getUUID(@CookieParam("uuid") Optional<UUIDParam> uuid) {
            return uuid.or(new UUIDParam("d5672fa8-326b-40f6-bf71-d9dacf44bcdc")).get().toString();
        }
    }
}
