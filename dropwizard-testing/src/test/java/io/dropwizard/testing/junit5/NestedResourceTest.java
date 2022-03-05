package io.dropwizard.testing.junit5;

import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class NestedResourceTest {
    private static final ConfigurationSourceProvider resourceConfigurationSourceProvider = new ResourceConfigurationSourceProvider();
    private static final DropwizardAppExtension<TestConfiguration> staticApp = new DropwizardAppExtension<>(
            TestApplication.class, "test-config.yaml", resourceConfigurationSourceProvider);
    private static final DropwizardClientExtension staticClient = new DropwizardClientExtension();
    private static final DAOTestExtension staticDao = DAOTestExtension.newBuilder().build();
    private static final ResourceExtension staticResources = ResourceExtension.builder().build();

    private final DropwizardAppExtension<TestConfiguration> app = new DropwizardAppExtension<>(
            TestApplication.class, "test-config.yaml", resourceConfigurationSourceProvider);
    private final DropwizardClientExtension client = new DropwizardClientExtension();
    private final DAOTestExtension dao = DAOTestExtension.newBuilder().build();
    private final ResourceExtension resources = ResourceExtension.builder().build();

    @Test
    void staticApp() {
        assertThat(staticApp.getEnvironment()).isNotNull();
    }

    @Test
    void staticClient() {
        assertThat(staticClient.baseUri()).isNotNull();
    }

    @Test
    void staticDao() {
        assertThat(staticDao.getSessionFactory()).isNotNull();
    }

    @Test
    void staticResources() {
        assertThat(staticResources.target("")).isNotNull();
    }

    @Test
    void app() {
        assertThat(app.getEnvironment()).isNotNull();
    }

    @Test
    void client() {
        assertThat(client.baseUri()).isNotNull();
    }

    @Test
    void dao() {
        assertThat(dao.getSessionFactory()).isNotNull();
    }

    @Test
    void resources() {
        assertThat(resources.target("")).isNotNull();
    }

    @Nested
    class InnerTest {
        @Test
        void staticApp() {
            assertThat(staticApp.getEnvironment()).isNotNull();
        }

        @Test
        void staticClient() {
            assertThat(staticClient.baseUri()).isNotNull();
        }

        @Test
        void staticDao() {
            assertThat(staticDao.getSessionFactory()).isNotNull();
        }

        @Test
        void staticResources() {
            assertThat(staticResources.target("")).isNotNull();
        }

        @Test
        void app() {
            assertThat(app.getEnvironment()).isNotNull();
        }

        @Test
        void client() {
            assertThat(client.baseUri()).isNotNull();
        }

        @Test
        void dao() {
            assertThat(dao.getSessionFactory()).isNotNull();
        }

        @Test
        void resources() {
            assertThat(resources.target("")).isNotNull();
        }

        @Nested
        class InnerInnerTest {
            @Test
            void staticApp() {
                assertThat(staticApp.getEnvironment()).isNotNull();
            }

            @Test
            void staticClient() {
                assertThat(staticClient.baseUri()).isNotNull();
            }

            @Test
            void staticDao() {
                assertThat(staticDao.getSessionFactory()).isNotNull();
            }

            @Test
            void staticResources() {
                assertThat(staticResources.target("")).isNotNull();
            }

            @Test
            void app() {
                assertThat(app.getEnvironment()).isNotNull();
            }

            @Test
            void client() {
                assertThat(client.baseUri()).isNotNull();
            }

            @Test
            void dao() {
                assertThat(dao.getSessionFactory()).isNotNull();
            }

            @Test
            void resources() {
                assertThat(resources.target("")).isNotNull();
            }
        }
    }
}
