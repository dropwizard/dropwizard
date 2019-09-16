package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class NestedResourceTest {
    private static DropwizardAppExtension<TestConfiguration> staticApp = new DropwizardAppExtension<>(
            TestApplication.class, resourceFilePath("test-config.yaml"));
    private static DropwizardClientExtension staticClient = new DropwizardClientExtension();
    private static DAOTestExtension staticDao = DAOTestExtension.newBuilder().build();
    private static ResourceExtension staticResources = ResourceExtension.builder().build();

    private DropwizardAppExtension<TestConfiguration> app = new DropwizardAppExtension<>(
            TestApplication.class, resourceFilePath("test-config.yaml"));
    private DropwizardClientExtension client = new DropwizardClientExtension();
    private DAOTestExtension dao = DAOTestExtension.newBuilder().build();
    private ResourceExtension resources = ResourceExtension.builder().build();

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