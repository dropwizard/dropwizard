package io.dropwizard.testing.junit5;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import java.util.Optional;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

abstract class AbstractDropwizardAppExtensionTest {

    @Test
    void canGetExpectedResourceOverHttp() {
        final String content = JerseyClientBuilder.createClient().target(
                "http://localhost:" + getExtension().getLocalPort() + "/test").request().get(String.class);

        assertThat(content).isEqualTo("Yes, it's here");
    }

    @Test
    void returnsConfiguration() {
        final TestConfiguration config = getExtension().getConfiguration();
        assertThat(config.getMessage()).isEqualTo("Yes, it's here");
    }

    @Test
    void returnsApplication() {
        final DropwizardTestApplication application = getExtension().getApplication();
        Assertions.assertNotNull(application);
    }

    @Test
    void returnsEnvironment() {
        final Environment environment = getExtension().getEnvironment();
        assertThat(environment.getName()).isEqualTo("DropwizardTestApplication");
    }

    @Test
    void canPerformAdminTask() {
        final String response
                = getExtension().client().target("http://localhost:"
                + getExtension().getAdminPort() + "/tasks/hello?name=test_user")
                .request()
                .post(Entity.entity("", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Hello has been said to test_user");
    }

    @Test
    void canPerformAdminTaskWithPostBody() {
        final String response = getExtension().client()
                .target("http://localhost:" + getExtension().getAdminPort() + "/tasks/echo")
                .request()
                .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }

    @Test
    void clientUsesJacksonMapperFromEnvironment() {
        final Optional<String> message = getExtension().client()
                .target("http://localhost:" + getExtension().getLocalPort() + "/message")
                .request()
                .get(DropwizardTestApplication.MessageView.class)
                .getMessage();
        assertThat(message)
                .hasValue("Yes, it's here");
    }

    @Test
    void clientSupportsPatchMethod() {
        final String method = getExtension().client()
                .target("http://localhost:" + getExtension().getLocalPort() + "/echoPatch")
                .request()
                .method("PATCH", Entity.text("Patch is working"), String.class);
        assertThat(method).isEqualTo("Patch is working");
    }

    abstract DropwizardAppExtension<TestConfiguration> getExtension();
}
