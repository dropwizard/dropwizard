package io.dropwizard.testing.junit5;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.extension.RegisterExtension;

class DropwizardAppExtensionRegisterExtensionTest extends AbstractDropwizardAppExtensionTest {

    @RegisterExtension
    public static final DropwizardAppExtension<TestConfiguration> EXTENSION =
            new DropwizardAppExtension<>(DropwizardTestApplication.class,
                "test-config.yaml", new ResourceConfigurationSourceProvider());

    @Override
    DropwizardAppExtension<TestConfiguration> getExtension() {
        return EXTENSION;
    }
}
