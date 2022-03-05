package io.dropwizard.testing.junit5;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

class ReuseDropwizardAppExtensionTestSuite {
    static final DropwizardAppExtension<TestConfiguration> EXTENSION =
        new DropwizardAppExtension<>(DropwizardTestApplication.class, "test-config.yaml",
            new ResourceConfigurationSourceProvider());

}

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionTestSuiteFooTest {
    static final DropwizardAppExtension<TestConfiguration> EXTENSION = ReuseDropwizardAppExtensionTestSuite.EXTENSION;

    @Test
    void clientHasNotBeenClosed() {
        final String response = EXTENSION.client()
                .target("http://localhost:" + EXTENSION.getAdminPort() + "/tasks/echo")
                .request()
                .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }
}

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionTestSuiteBarTest {
    static final DropwizardAppExtension<TestConfiguration> EXTENSION = ReuseDropwizardAppExtensionTestSuite.EXTENSION;

    @Test
    void clientHasNotBeenClosed() {
        final String response = EXTENSION.client()
                .target("http://localhost:" + EXTENSION.getAdminPort() + "/tasks/echo")
                .request()
                .post(Entity.entity("Custom message", MediaType.TEXT_PLAIN), String.class);

        assertThat(response).isEqualTo("Custom message");
    }
}

