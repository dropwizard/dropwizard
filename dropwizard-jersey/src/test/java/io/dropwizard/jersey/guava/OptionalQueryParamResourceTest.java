package io.dropwizard.jersey.guava;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.MyMessage;
import io.dropwizard.jersey.MyMessageParamConverterProvider;
import io.dropwizard.jersey.params.UUIDParam;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalQueryParamResourceTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .register(OptionalQueryParamResource.class)
                .register(MyMessageParamConverterProvider.class);
    }

    @Test
    public void shouldReturnDefaultMessageWhenMessageIsNotPresent() {
        String defaultMessage = "Default Message";
        String response = target("/optional/message").request().get(String.class);
        assertThat(response).isEqualTo(defaultMessage);
    }

    @Test
    public void shouldReturnMessageWhenMessageIsPresent() {
        String customMessage = "Custom Message";
        String response = target("/optional/message").queryParam("message", customMessage).request().get(String.class);
        assertThat(response).isEqualTo(customMessage);
    }

    @Test
    public void shouldReturnMessageWhenMessageIsBlank() {
        String response = target("/optional/message").queryParam("message", "").request().get(String.class);
        assertThat(response).isEqualTo("");
    }

    @Test
    public void shouldReturnDecodedMessageWhenEncodedMessageIsPresent() {
        String encodedMessage = "Custom%20Message";
        String decodedMessage = "Custom Message";
        String response = target("/optional/message").queryParam("message", encodedMessage).request().get(String.class);
        assertThat(response).isEqualTo(decodedMessage);
    }

    @Test
    public void shouldReturnDefaultMessageWhenMyMessageIsNotPresent() {
        String defaultMessage = "My Default Message";
        String response = target("/optional/my-message").request().get(String.class);
        assertThat(response).isEqualTo(defaultMessage);
    }

    @Test
    public void shouldReturnMyMessageWhenMyMessageIsPresent() {
        String myMessage = "My Message";
        String response = target("/optional/my-message").queryParam("mymessage", myMessage).request().get(String.class);
        assertThat(response).isEqualTo(myMessage);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidUUIDIsPresent() {
        String invalidUUID = "invalid-uuid";
        target("/optional/uuid").queryParam("uuid", invalidUUID).request().get(String.class);
    }

    @Test
    public void shouldReturnDefaultUUIDWhenUUIDIsNotPresent() {
        String defaultUUID = "d5672fa8-326b-40f6-bf71-d9dacf44bcdc";
        String response = target("/optional/uuid").request().get(String.class);
        assertThat(response).isEqualTo(defaultUUID);
    }

    @Test
    public void shouldReturnUUIDWhenValidUUIDIsPresent() {
        String uuid = "fd94b00d-bd50-46b3-b42f-905a9c9e7d78";
        String response = target("/optional/uuid").queryParam("uuid", uuid).request().get(String.class);
        assertThat(response).isEqualTo(uuid);
    }

    @Test
    public void shouldReturnDefaultValueWhenValueIsAbsent() {
        String response = target("/optional/value").request().get(String.class);
        assertThat(response).isEqualTo("42");
    }

    @Test
    public void shouldReturnDefaultValueWhenEmptyValueIsGiven() {
        String response = target("/optional/value").queryParam("value", "").request().get(String.class);
        assertThat(response).isEqualTo("42");
    }

    @Test
    public void shouldReturnValueWhenValueIsPresent() {
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
