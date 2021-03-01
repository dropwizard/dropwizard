package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.DropwizardTestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionTest extends AbstractDropwizardAppExtensionTest {

    private static final DropwizardAppExtension<TestConfiguration> EXTENSION =
            new DropwizardAppExtension<>(DropwizardTestApplication.class, resourceFilePath("test-config.yaml"));

    @Override
    DropwizardAppExtension<TestConfiguration> getExtension() {
        return EXTENSION;
    }
}
