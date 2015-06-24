package io.dropwizard.testing.app;

import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.testing.junit.TestApplicationExceptionMappers;
import io.dropwizard.testing.junit.TestConfiguration;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to make sure that the default exception mappers can be overridden
 * easily in an application.
 */
public class OverrideDefaultExceptionMapperTest {
    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<>(TestApplicationExceptionMappers.class, resourceFilePath("test-config.yaml"));

    @Test
    public void testDefaultExceptionMappersAreOverridden() {
        final Response clientResponse = ClientBuilder.newClient()
                .target("http://localhost:" + RULE.getLocalPort() + "/")
                .request().post(Entity.json(""));

        assertThat(clientResponse.getStatus()).isEqualTo(204);
    }
}
