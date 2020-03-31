package io.dropwizard.testing.junit5;

import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Optional;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardAppExtensionTest {
    public static final DropwizardAppExtension<TestConfiguration> EXTENSION =
            new DropwizardAppExtension<>(DropwizardTestApplication.class, resourceFilePath("test-config.yaml"));

    @Test
    public void canGetExpectedResourceOverHttp() {
        final String content = ClientBuilder.newClient().target(
                "http://localhost:" + EXTENSION.getLocalPort() + "/test").request().get(String.class);

        assertThat(content).isEqualTo("Yes, it's here");
    }

    @Test
    public void returnsConfiguration() {
        final TestConfiguration config = EXTENSION.getConfiguration();
        assertThat(config.getMessage()).isEqualTo("Yes, it's here");
    }

    @Test
    public void returnsApplication() {
        final DropwizardTestApplication application = EXTENSION.getApplication();
        Assertions.assertNotNull(application);
    }

    @Test
    public void returnsEnvironment() {
        final Environment environment = EXTENSION.getEnvironment();
        assertThat(environment.getName()).isEqualTo("DropwizardTestApplication");
    }

    @Test
    public void canPerformAdminTask() {
        final String response
                = EXTENSION.client().target("http://localhost:"
                + EXTENSION.getAdminPort() + "/tasks/hello?name=test_user")
                .request()
                .post(Entity.entity("", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Hello has been said to test_user");
    }

    @Test
    public void canPerformAdminTaskWithPostBody() {
        final String response = EXTENSION.client()
                .target("http://localhost:" + EXTENSION.getAdminPort() + "/tasks/echo")
                .request()
                .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }

    @Test
    public void clientUsesJacksonMapperFromEnvironment() {
        final Optional<String> message = EXTENSION.client()
                .target("http://localhost:" + EXTENSION.getLocalPort() + "/message")
                .request()
                .get(DropwizardTestApplication.MessageView.class)
                .getMessage();
        assertThat(message)
                .hasValue("Yes, it's here");
    }

    @Test
    public void clientSupportsPatchMethod() {
        final String method = EXTENSION.client()
                .target("http://localhost:" + EXTENSION.getLocalPort() + "/echoPatch")
                .request()
                .method("PATCH", Entity.text("Patch is working"), String.class);
        assertThat(method).isEqualTo("Patch is working");
    }
}
