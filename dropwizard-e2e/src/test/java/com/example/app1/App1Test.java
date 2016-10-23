package com.example.app1;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class App1Test {
    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE =
        new DropwizardAppRule<>(App1.class, ResourceHelpers.resourceFilePath("app1/config.yml"));

    @Test
    public void custom204OnEmptyOptional() {
        final Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client");
        final String url = String.format("http://localhost:%d/empty-optional", RULE.getLocalPort());

        final Response response = client.target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(204);
    }
}
