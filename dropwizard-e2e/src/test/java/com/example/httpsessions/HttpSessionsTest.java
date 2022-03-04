package com.example.httpsessions;

import io.dropwizard.core.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class HttpSessionsTest {
    public static final DropwizardAppExtension<Configuration> RULE =
        new DropwizardAppExtension<>(HttpSessionsApp.class, "httpsessions/config.yml", new ResourceConfigurationSourceProvider());

    @Test
    void testInjectedSessionsIsNotNull() {
        Boolean sessionNotNull = RULE.client().target(String.format("http://localhost:%d/session", RULE.getLocalPort())).request().get(Boolean.class);
        assertThat(sessionNotNull).isNotNull().isTrue();
    }
}
