package io.dropwizard.jersey.guava;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.Application;

import static org.fest.assertions.api.Assertions.assertThat;

public class OptionalQueryParamResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        ResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        config.register(new OptionalQueryParamResource());
        config.register(new OptionalQueryParamValueFactoryProvider.Binder());
        config.register(new MyMessageParamConveterProvider());
        return config;
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
}
