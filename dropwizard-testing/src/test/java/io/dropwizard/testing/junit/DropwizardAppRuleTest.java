package io.dropwizard.testing.junit;

import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardAppRuleTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
        new DropwizardAppRule<>(DropwizardTestApplication.class, resourceFilePath("test-config.yaml"));

    @Test
    public void canGetExpectedResourceOverHttp() {
        final String content = ClientBuilder.newClient().target(
            "http://localhost:" + RULE.getLocalPort() + "/test").request().get(String.class);

        assertThat(content).isEqualTo("Yes, it's here");
    }

    @Test
    public void returnsConfiguration() {
        final TestConfiguration config = RULE.getConfiguration();
        assertThat(config.getMessage()).isEqualTo("Yes, it's here");
    }

    @Test
    public void returnsApplication() {
        final DropwizardTestApplication application = RULE.getApplication();
        assertThat(application).isNotNull();
    }

    @Test
    public void returnsEnvironment() {
        final Environment environment = RULE.getEnvironment();
        assertThat(environment.getName()).isEqualTo("DropwizardTestApplication");
    }

    @Test
    public void canPerformAdminTask() {
        final String response
            = RULE.client().target("http://localhost:"
            + RULE.getAdminPort() + "/tasks/hello?name=test_user")
            .request()
            .post(Entity.entity("", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Hello has been said to test_user");
    }

    @Test
    public void canPerformAdminTaskWithPostBody() {
        final String response
            = RULE.client().target("http://localhost:"
            + RULE.getAdminPort() + "/tasks/echo")
            .request()
            .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }

    @Test
    public void clientUsesJacksonMapperFromEnvironment() {
        assertThat(RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/message")
            .request()
            .get(DropwizardTestApplication.MessageView.class).getMessage())
            .contains("Yes, it's here");
    }

    @Test
    public void clientSupportsPatchMethod() {
        assertThat(RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/echoPatch")
            .request()
            .method("PATCH", Entity.text("Patch is working"), String.class))
            .contains("Patch is working");
    }
}
