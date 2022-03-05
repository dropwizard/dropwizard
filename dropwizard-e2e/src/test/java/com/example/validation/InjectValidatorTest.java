package com.example.validation;

import io.dropwizard.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class InjectValidatorTest {

    public static final DropwizardAppExtension<Configuration> RULE = new DropwizardAppExtension<>(
            DefaultValidatorApp.class,
        "app1/config.yml", new ResourceConfigurationSourceProvider()
    );

    @Test
    void shouldValidateNormally() {
        final Client client = RULE.client();
        final String url = String.format("http://localhost:%d/default", RULE.getLocalPort());
        final WebTarget target = client.target(url);

        Response validRequest = target
            .queryParam("value", "right")
            .request().get();

        Response invalidRequest = target
            .queryParam("value", "wrong")
            .request().get();

        assertThat(validRequest.getStatus()).isEqualTo(204);

        assertThat(invalidRequest.getStatus()).isEqualTo(400);
        assertThat(invalidRequest.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"query param value must be one of [right]\"]}");
    }

    @Test
    void shouldInjectValidator() {
        final Client client = RULE.client();
        final String url = String.format("http://localhost:%d/injectable", RULE.getLocalPort());

        final Response response = client.target(url)
            .queryParam("value", "right")
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(204);
    }
}
