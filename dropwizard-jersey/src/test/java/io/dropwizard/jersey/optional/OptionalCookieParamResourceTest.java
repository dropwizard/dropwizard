package io.dropwizard.jersey.optional;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.MyMessage;
import io.dropwizard.jersey.MyMessageParamConverterProvider;
import io.dropwizard.jersey.params.UUIDParam;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.CookieParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalCookieParamResourceTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .register(OptionalCookieParamResource.class)
                .register(MyMessageParamConverterProvider.class);
    }

    @Test
    public void shouldReturnDefaultMessageWhenMessageIsNotPresent() {
        String defaultMessage = "Default Message";
        String response = target("/optional/message").request().get(String.class);
        assertThat(response).isEqualTo(defaultMessage);
    }

    @Test
    public void shouldReturnMessageWhenMessageIsBlank() {
        String response = target("/optional/message").request().cookie("message", "").get(String.class);
        assertThat(response).isEqualTo("");
    }

    @Test
    public void shouldReturnMessageWhenMessageIsPresent() {
        String customMessage = "Custom Message";
        String response = target("/optional/message").request().cookie("message", customMessage).get(String.class);
        assertThat(response).isEqualTo(customMessage);
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
        String response = target("/optional/my-message").request().cookie("mymessage", myMessage).get(String.class);
        assertThat(response).isEqualTo(myMessage);
    }

    @Test(expected = BadRequestException.class)
    public void shouldThrowBadRequestExceptionWhenInvalidUUIDIsPresent() {
        String invalidUUID = "invalid-uuid";
        target("/optional/uuid").request().cookie("uuid", invalidUUID).get(String.class);
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
        String response = target("/optional/uuid").request().cookie("uuid", uuid).get(String.class);
        assertThat(response).isEqualTo(uuid);
    }

    @Path("/optional")
    public static class OptionalCookieParamResource {
        @GET
        @Path("/message")
        public String getMessage(@CookieParam("message") Optional<String> message) {
            return message.orElse("Default Message");
        }

        @GET
        @Path("/my-message")
        public String getMyMessage(@CookieParam("mymessage") Optional<MyMessage> myMessage) {
            return myMessage.orElse(new MyMessage("My Default Message")).getMessage();
        }

        @GET
        @Path("/uuid")
        public String getUUID(@CookieParam("uuid") Optional<UUIDParam> uuid) {
            return uuid.orElse(new UUIDParam("d5672fa8-326b-40f6-bf71-d9dacf44bcdc")).get().toString();
        }
    }
}
