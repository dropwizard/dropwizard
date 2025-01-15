package io.dropwizard.testing.junit5;

import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ConfigOverride.randomPorts;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionRandomPortsConfigOverrideTest {

    private static final DropwizardAppExtension<TestConfiguration> EXTENSION =
        new DropwizardAppExtension<>(TestApplication.class,
            null,
            "app-rule",
            randomPorts("app-rule"),
            config("app-rule", "message", "A new way to say Hooray!"),
            config("app-rule", "extra", () -> "supplied"));

    @Test
    void supportsRandomPortsConfigAttributeOverrides() {
        DefaultServerFactory serverFactory = (DefaultServerFactory) EXTENSION.getConfiguration()
            .getServerFactory();

        assertThat(serverFactory.getApplicationConnectors())
            .map(HttpConnectorFactory.class::cast)
            .extracting(HttpConnectorFactory::getPort)
            .containsExactly(0);
        assertThat(serverFactory.getAdminConnectors())
            .map(HttpConnectorFactory.class::cast)
            .extracting(HttpConnectorFactory::getPort)
            .containsExactly(0);
    }
}
