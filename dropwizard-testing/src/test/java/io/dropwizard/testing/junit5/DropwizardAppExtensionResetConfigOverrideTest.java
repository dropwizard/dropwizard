package io.dropwizard.testing.junit5;


import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;

import static io.dropwizard.testing.ConfigOverride.config;
import static org.assertj.core.api.Assertions.assertThat;

class DropwizardAppExtensionResetConfigOverrideTest {
    private final DropwizardAppExtension<TestConfiguration> dropwizardAppExtension = new DropwizardAppExtension<>(
            TestApplication.class,
            "test-config.yaml",
            new ResourceConfigurationSourceProvider(),
            "app-rule-reset",
            config("app-rule-reset", "message", "A new way to say Hooray!"));

    @Test
    void test2() throws Exception {
        dropwizardAppExtension.before();
        assertThat(System.getProperty("app-rule-reset.message")).isEqualTo("A new way to say Hooray!");
        assertThat(System.getProperty("app-rule-reset.extra")).isNull();
        dropwizardAppExtension.after();

        System.setProperty("app-rule-reset.extra", "Some extra system property");
        dropwizardAppExtension.before();
        assertThat(System.getProperty("app-rule-reset.message")).isEqualTo("A new way to say Hooray!");
        assertThat(System.getProperty("app-rule-reset.extra")).isEqualTo("Some extra system property");
        dropwizardAppExtension.after();

        assertThat(System.getProperty("app-rule-reset.message")).isNull();
        assertThat(System.getProperty("app-rule-reset.extra")).isEqualTo("Some extra system property");
        System.clearProperty("app-rule-reset.extra");
    }
}
