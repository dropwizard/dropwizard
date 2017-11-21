package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardAppExtensionConfigOverrideTest {

    public static final DropwizardAppExtension<TestConfiguration> EXTENSION =
        new DropwizardAppExtension<>(TestApplication.class, resourceFilePath("test-config.yaml"),
            Optional.of("app-rule"),
            config("app-rule", "message", "A new way to say Hooray!"),
            config("app-rule", "extra", () -> "supplied"),
            config("extra", () -> "supplied again"));

    @Test
    public void supportsConfigAttributeOverrides() {
        final String content = EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/test")
            .request().get(String.class);

        assertThat(content).isEqualTo("A new way to say Hooray!");
    }

    @Test
    public void supportsSuppliedConfigAttributeOverrides() throws Exception {
        assertThat(System.getProperty("app-rule.extra")).isEqualTo("supplied");
        assertThat(System.getProperty("dw.extra")).isEqualTo("supplied again");
    }
}
