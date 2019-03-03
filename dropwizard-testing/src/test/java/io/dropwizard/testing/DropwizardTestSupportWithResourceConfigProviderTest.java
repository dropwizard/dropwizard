package io.dropwizard.testing;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DropwizardTestSupportWithResourceConfigProviderTest {
    private static final DropwizardTestSupport<TestConfiguration> TEST_SUPPORT = new DropwizardTestSupport<>(
            TestApplication.class, "test-config.yaml", new ResourceConfigurationSourceProvider());

    @BeforeAll
    static void setUp() throws Exception {
        TEST_SUPPORT.before();
    }

    @AfterAll
    static void tearDown() {
        TEST_SUPPORT.after();
    }

    @Test
    void returnsConfiguration() {
        final TestConfiguration config = TEST_SUPPORT.getConfiguration();
        assertThat(config.getMessage()).isEqualTo("Yes, it's here");
    }

    @Test
    void returnsApplication() {
        final TestApplication application = TEST_SUPPORT.getApplication();
        assertThat(application).isNotNull();
    }

    @Test
    void returnsEnvironment() {
        final Environment environment = TEST_SUPPORT.getEnvironment();
        assertThat(environment.getName()).isEqualTo("TestApplication");
    }
}
