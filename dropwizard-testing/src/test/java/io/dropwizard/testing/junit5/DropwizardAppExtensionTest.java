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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardAppExtensionTest {
    public static final DropwizardAppExtension<TestConfiguration> EXTENSION =
        new DropwizardAppExtension<>(DropwizardTestApplication.class, resourceFilePath("test-config.yaml"));

    @Test
    public void canGetExpectedResourceOverHttp() {
        final String content = ClientBuilder.newClient().target(
            "http://localhost:" + EXTENSION.getLocalPort() + "/test").request().get(String.class);

        assertThat(content, is("Yes, it's here"));
    }

    @Test
    public void returnsConfiguration() {
        final TestConfiguration config = EXTENSION.getConfiguration();
        assertThat(config.getMessage(), is("Yes, it's here"));
    }

    @Test
    public void returnsApplication() {
        final DropwizardTestApplication application = EXTENSION.getApplication();
        Assertions.assertNotNull(application);
    }

    @Test
    public void returnsEnvironment() {
        final Environment environment = EXTENSION.getEnvironment();
        assertThat(environment.getName(), is("DropwizardTestApplication"));
    }

    @Test
    public void canPerformAdminTask() {
        final String response
            = EXTENSION.client().target("http://localhost:"
            + EXTENSION.getAdminPort() + "/tasks/hello?name=test_user")
            .request()
            .post(Entity.entity("", MediaType.TEXT_PLAIN), String.class);

        assertThat(response, is("Hello has been said to test_user"));
    }

    @Test
    public void canPerformAdminTaskWithPostBody() {
        final String response
            = EXTENSION.client().target("http://localhost:"
            + EXTENSION.getAdminPort() + "/tasks/echo")
            .request()
            .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response, is("Custom message"));
    }

    @Test
    public void clientUsesJacksonMapperFromEnvironment() {
        assertThat(EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/message")
            .request()
            .get(DropwizardTestApplication.MessageView.class).getMessage(), is(Optional.of("Yes, it's here")));
    }

    @Test
    public void clientSupportsPatchMethod() {
        assertThat(EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/echoPatch")
            .request()
            .method("PATCH", Entity.text("Patch is working"), String.class), is("Patch is working"));
    }

}
