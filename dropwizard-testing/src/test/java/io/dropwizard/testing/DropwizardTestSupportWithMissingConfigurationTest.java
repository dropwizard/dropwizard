package io.dropwizard.testing;

import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DropwizardTestSupportWithMissingConfigurationTest {
    @Test
    void configurationDoesNotExist() {
        final DropwizardTestSupport<TestConfiguration> testSupport =
                new DropwizardTestSupport<>(TestApplication.class, "not-found.yaml");

        assertThatThrownBy(testSupport::before)
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("File not-found.yaml not found");
    }
}
