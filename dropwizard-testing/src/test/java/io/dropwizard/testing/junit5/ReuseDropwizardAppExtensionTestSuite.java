package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class ReuseDropwizardAppExtensionTestSuite {
    public static final DropwizardAppExtension<TestConfiguration> EXTENSION =
        new DropwizardAppExtension<>(DropwizardTestApplication.class, resourceFilePath("test-config.yaml"));

}

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionTestSuiteFoo {
    public static final DropwizardAppExtension<TestConfiguration> EXTENSION = ReuseDropwizardAppExtensionTestSuite.EXTENSION;

    @Test
    public void clientHasNotBeenClosed() {
        final String response = EXTENSION.client()
                .target("http://localhost:" + EXTENSION.getAdminPort() + "/tasks/echo")
                .request()
                .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }
}

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionTestSuiteBar {
    public static final DropwizardAppExtension<TestConfiguration> EXTENSION = ReuseDropwizardAppExtensionTestSuite.EXTENSION;

    @Test
    public void clientHasNotBeenClosed() {
        final String response = EXTENSION.client()
                .target("http://localhost:" + EXTENSION.getAdminPort() + "/tasks/echo")
                .request()
                .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }
}

