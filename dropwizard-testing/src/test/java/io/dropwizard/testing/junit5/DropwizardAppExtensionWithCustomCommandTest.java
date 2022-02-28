package io.dropwizard.testing.junit5;

import io.dropwizard.cli.Command;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionWithCustomCommandTest {

    private static final DropwizardAppExtension<TestConfiguration> EXTENSION = new DropwizardAppExtension<>(
        DropwizardTestApplication.class, "test-config.yaml", new ResourceConfigurationSourceProvider(), null,
        application -> new Command("test", "Test command") {
            @Override
            public void configure(Subparser subparser) {
            }

            @Override
            public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
            }
        });


    @Test
    void configurationIsNull() {
        assertThatNullPointerException().isThrownBy(EXTENSION::getConfiguration);
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
