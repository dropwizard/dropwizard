package io.dropwizard.testing;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import java.io.FileNotFoundException;
import org.junit.jupiter.api.Test;

class DropwizardTestSupportWithMissingConfigurationTest {
    @Test
    void configurationDoesNotExist() {
        final DropwizardTestSupport<TestConfiguration> testSupport =
                new DropwizardTestSupport<>(TestApplication.class, "not-found.yaml");

        assertThatExceptionOfType(FileNotFoundException.class)
                .isThrownBy(testSupport::before)
                .withMessage("File not-found.yaml not found");
    }
}
