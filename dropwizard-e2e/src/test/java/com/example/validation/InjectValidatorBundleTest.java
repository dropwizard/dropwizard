package com.example.validation;

import io.dropwizard.Configuration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class InjectValidatorBundleTest {

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(
        InjectValidatorApp.class,
        resourceFilePath("app1/config.yml")
    );

    @Test
    public void shouldValidateNormally() {
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
    public void shouldInjectValidator() {
        final Client client = RULE.client();
        final String url = String.format("http://localhost:%d/injectable", RULE.getLocalPort());

        final Response response = client.target(url)
            .queryParam("value", "right")
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(204);
    }
}
