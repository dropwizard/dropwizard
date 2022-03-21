package io.dropwizard.testing.junit5;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.cli.CheckCommand;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionWithCheckCommandTest {

    private static final DropwizardAppExtension<TestConfiguration> EXTENSION = new DropwizardAppExtension<>(
        DropwizardTestApplication.class, "test-config.yaml", new ResourceConfigurationSourceProvider(), null, CheckCommand::new);


    @Test
    void returnsConfiguration() {
        assertThat(EXTENSION.getConfiguration().getMessage()).isEqualTo("Yes, it's here");
    }

    @Test
    void returnsApplication() {
        assertThat(EXTENSION.<DropwizardTestApplication>getApplication()).isNotNull();
    }

    @Test
    void environmentIsNull() {
        assertThatNullPointerException().isThrownBy(EXTENSION::getEnvironment);
    }
}
