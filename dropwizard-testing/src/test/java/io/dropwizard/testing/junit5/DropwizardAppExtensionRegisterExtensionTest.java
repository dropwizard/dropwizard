package io.dropwizard.testing.junit5;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.extension.RegisterExtension;

class DropwizardAppExtensionRegisterExtensionTest extends AbstractDropwizardAppExtensionTest {

    @RegisterExtension
    public static final DropwizardAppExtension<TestConfiguration> EXTENSION =
            new DropwizardAppExtension<>(DropwizardTestApplication.class, resourceFilePath("test-config.yaml"));

    @Override
    DropwizardAppExtension<TestConfiguration> getExtension() {
        return EXTENSION;
    }
}
