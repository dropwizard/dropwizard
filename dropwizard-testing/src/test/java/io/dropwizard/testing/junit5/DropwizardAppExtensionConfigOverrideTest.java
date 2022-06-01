package io.dropwizard.testing.junit5;

import static io.dropwizard.testing.ConfigOverride.config;
import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionConfigOverrideTest {

    private static final DropwizardAppExtension<TestConfiguration> EXTENSION = new DropwizardAppExtension<>(
            TestApplication.class,
            "test-config.yaml",
            new ResourceConfigurationSourceProvider(),
            "app-rule",
            config("app-rule", "message", "A new way to say Hooray!"),
            config("app-rule", "extra", () -> "supplied"),
            config("extra", () -> "supplied again"));

    @Test
    void supportsConfigAttributeOverrides() {
        final String content = EXTENSION
                .client()
                .target("http://localhost:" + EXTENSION.getLocalPort() + "/test")
                .request()
                .get(String.class);

        assertThat(content).isEqualTo("A new way to say Hooray!");
    }

    @Test
    void supportsSuppliedConfigAttributeOverrides() {
        assertThat(System.getProperties())
                .containsEntry("app-rule.extra", "supplied")
                .containsEntry("dw.extra", "supplied again");
    }
}
