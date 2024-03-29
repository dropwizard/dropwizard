package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.MyMessage;
import io.dropwizard.jersey.MyMessageParamConverterProvider;
import io.dropwizard.jersey.params.UUIDParam;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Application;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OptionalQueryParamResourceTest extends AbstractJerseyTest {

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .register(OptionalQueryParamResource.class)
                .register(MyMessageParamConverterProvider.class);
    }

    @Test
    void shouldReturnDefaultMessageWhenMessageIsNotPresent() {
        String defaultMessage = "Default Message";
        String response = target("/optional/message").request().get(String.class);
        assertThat(response).isEqualTo(defaultMessage);
    }

    @Test
    void shouldReturnMessageWhenMessageIsPresent() {
        String customMessage = "Custom Message";
        String response = target("/optional/message").queryParam("message", customMessage).request().get(String.class);
        assertThat(response).isEqualTo(customMessage);
    }

    @Test
    void shouldReturnMessageWhenMessageIsBlank() {
        String response = target("/optional/message").queryParam("message", "").request().get(String.class);
        assertThat(response).isEmpty();
    }

    @Test
    void shouldReturnDecodedMessageWhenEncodedMessageIsPresent() {
        String encodedMessage = "Custom%20Message";
        String decodedMessage = "Custom Message";
        String response = target("/optional/message").queryParam("message", encodedMessage).request().get(String.class);
        assertThat(response).isEqualTo(decodedMessage);
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
        String response = target("/optional/my-message").queryParam("mymessage", myMessage).request().get(String.class);
        assertThat(response).isEqualTo(myMessage);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInvalidUUIDIsPresent() {
        String invalidUUID = "invalid-uuid";
        assertThatExceptionOfType(BadRequestException.class).isThrownBy(() ->
            target("/optional/uuid").queryParam("uuid", invalidUUID).request().get(String.class));
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
        String response = target("/optional/uuid").queryParam("uuid", uuid).request().get(String.class);
        assertThat(response).isEqualTo(uuid);
    }

    @Test
    void shouldReturnDefaultValueWhenValueIsAbsent() {
        String response = target("/optional/value").request().get(String.class);
        assertThat(response).isEqualTo("42");
    }

    @Test
    void shouldReturnDefaultValueWhenEmptyValueIsGiven() {
        String response = target("/optional/value").queryParam("value", "").request().get(String.class);
        assertThat(response).isEqualTo("42");
    }

    @Test
    void shouldReturnValueWhenValueIsPresent() {
        String value = "123456";
        String response = target("/optional/value").queryParam("value", value).request().get(String.class);
        assertThat(response).isEqualTo(value);
    }

    @Path("/optional")
    public static class OptionalQueryParamResource {

        @GET
        @Path("/message")
        public String getMessage(@QueryParam("message") Optional<String> message) {
            return message.or("Default Message");
        }

        @GET
        @Path("/my-message")
        public String getMyMessage(@QueryParam("mymessage") Optional<MyMessage> myMessage) {
            return myMessage.or(new MyMessage("My Default Message")).getMessage();
        }

        @GET
        @Path("/uuid")
        public String getUUID(@QueryParam("uuid") Optional<UUIDParam> uuid) {
            return uuid.or(new UUIDParam("d5672fa8-326b-40f6-bf71-d9dacf44bcdc")).get().toString();
        }

        @GET
        @Path("/value")
        public String getValue(@QueryParam("value") Optional<Integer> value) {
            return value.or(42).toString();
        }
    }
}
