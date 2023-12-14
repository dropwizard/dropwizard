package com.example.app1;

import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Configuration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.util.Duration;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.awaitility.Awaitility.await;

@ExtendWith(DropwizardExtensionsSupport.class)
public class App1Test {
    public static final DropwizardAppExtension<Configuration> RULE =
        new DropwizardAppExtension<>(App1.class, "app1/config.yml", new ResourceConfigurationSourceProvider());

    private static Client client;

    @BeforeAll
    public static void setup() {
        final JerseyClientConfiguration config = new JerseyClientConfiguration();
        // Avoid flakiness with default timeouts in CI builds
        config.setTimeout(Duration.seconds(5));
        client = new JerseyClientBuilder(RULE.getEnvironment())
            .withProvider(new CustomJsonProvider(Jackson.newObjectMapper()))
            .using(config)
            .build("test client");
    }

    @Test
    void custom204OnEmptyOptional() {
        final Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client 1");
        final String url = String.format("http://localhost:%d/empty-optional", RULE.getLocalPort());
        final Response response = client.target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    void custom404OnViewRenderMissingMustacheTemplate() {
        final Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client 2");
        final String url = String.format("http://localhost:%d/view-with-missing-tpl", RULE.getLocalPort());

        final Response response = client.target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    @Disabled("EOF is handled by Jetty since Jetty 12")
    void earlyEofTest() throws IOException {
        // Only eof test so we ensure it's false before test
        ((App1)RULE.getApplication()).wasEofExceptionHit = false;

        final URL url = new URL(String.format("http://localhost:%d/mapper", RULE.getLocalPort()));
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setFixedLengthStreamingMode(100000);

        conn.getOutputStream().write("{".getBytes(StandardCharsets.UTF_8));
        conn.disconnect();

        await().atMost(5, TimeUnit.SECONDS)
                .until(() -> ((App1) RULE.getApplication()).wasEofExceptionHit);
        assertThat(((App1) RULE.getApplication()).wasEofExceptionHit).isTrue();
    }

    @Test
    void customJsonProvider() {
        final String url = String.format("http://localhost:%d/mapper", RULE.getLocalPort());
        final String response = client.target(url)
            .request()
            .post(Entity.json("/** A Dropwizard specialty */\n{\"check\": \"mate\"}"), String.class);
        assertThat(response).isEqualTo("/** A Dropwizard specialty */\n" +
            "{\"check\":\"mate\",\"hello\":\"world\"}");
    }

    @Test
    void customJsonProviderMissingHeader() {
        final String url = String.format("http://localhost:%d/mapper", RULE.getLocalPort());
        final Response response = client.target(url)
            .request()
            .post(Entity.json("{\"check\": \"mate\"}"));
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void customJsonProviderClient() {
        final String url = String.format("http://localhost:%d/mapper", RULE.getLocalPort());
        final String response = client.target(url)
            .request()
            .post(Entity.json(Collections.singletonMap("check", "mate")), String.class);
        assertThat(response).isEqualTo("/** A Dropwizard specialty */\n" +
            "{\"check\":\"mate\",\"hello\":\"world\"}");
    }

    @Test
    void customJsonProviderRoundtrip() {
        final String url = String.format("http://localhost:%d/mapper", RULE.getLocalPort());
        final GenericType<Map<String, String>> typ = new GenericType<Map<String, String>>() {
        };

        final Map<String, String> response = client.target(url)
            .request()
            .post(Entity.json(Collections.singletonMap("check", "mate")), typ);
        assertThat(response).containsExactly(entry("check", "mate"), entry("hello", "world"));
    }

    @Test
    void customBodyWriterTest() {
        final String url = String.format("http://localhost:%d/custom-class", RULE.getLocalPort());
        final String response = client.target(url)
            .request()
            .get(String.class);
        assertThat(response).isEqualTo("I'm a custom class");
    }
}
