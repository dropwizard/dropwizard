package io.dropwizard.testing;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

class DropwizardTestSupportWithResourceConfigProviderTest {
    private static final TestResourceConfigurationSourceProvider TEST_CONFIG_SOURCE_PROVIDER =
            new TestResourceConfigurationSourceProvider();
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

    @Test
    void doesNotOverwriteConfigSourceProviderIfNotProvidedInConstructor() throws Exception {
        DropwizardTestSupport<TestConfiguration> support = new DropwizardTestSupport<>(
                TestApplication.class, "test-config.yaml");
        try {
            support.before();
            assertThat(TEST_CONFIG_SOURCE_PROVIDER.openCalled).isTrue();
        } finally {
            support.after();
        }
    }

    public static class TestResourceConfigurationSourceProvider extends ResourceConfigurationSourceProvider {
        volatile boolean openCalled = false;

        @Override
        public InputStream open(String path) throws IOException {
            openCalled = true;
            return super.open(path);
        }
    }

    public static class TestApplication extends io.dropwizard.testing.app.TestApplication {
        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.setConfigurationSourceProvider(TEST_CONFIG_SOURCE_PROVIDER);
        }
    }
}
