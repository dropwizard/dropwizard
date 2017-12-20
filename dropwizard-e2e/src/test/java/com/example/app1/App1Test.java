package com.example.app1;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class App1Test {
    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE =
        new DropwizardAppRule<>(App1.class, ResourceHelpers.resourceFilePath("app1/config.yml"));

    private static Client client;

    @BeforeClass
    public static void setup() {
        client = new JerseyClientBuilder(RULE.getEnvironment())
            .withProvider(new CustomJsonProvider(Jackson.newObjectMapper()))
            .build("test client");
    }

    @Test
    public void custom204OnEmptyOptional() {
        final Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client 1");
        final String url = String.format("http://localhost:%d/empty-optional", RULE.getLocalPort());
        final Response response = client.target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void custom404OnViewRenderMissingMustacheTemplate() {
        final Client client = new JerseyClientBuilder(RULE.getEnvironment()).build("test client 2");
        final String url = String.format("http://localhost:%d/view-with-missing-tpl", RULE.getLocalPort());

        final Response response = client.target(url).request().get();
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void earlyEofTest() throws IOException, InterruptedException {
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

        // Wait a bit for the app to process the request.
        Thread.sleep(500);
        assertThat(((App1)RULE.getApplication()).wasEofExceptionHit).isTrue();
    }

    @Test
    public void customJsonProvider() {
        final String url = String.format("http://localhost:%d/mapper", RULE.getLocalPort());
        final String response = client.target(url)
            .request()
            .post(Entity.json("/** A Dropwizard specialty */\n{\"check\": \"mate\"}"), String.class);
        assertThat(response).isEqualTo("/** A Dropwizard specialty */\n" +
            "{\"check\":\"mate\",\"hello\":\"world\"}");
    }

    @Test
    public void customJsonProviderMissingHeader() {
        final String url = String.format("http://localhost:%d/mapper", RULE.getLocalPort());
        final Response response = client.target(url)
            .request()
            .post(Entity.json("{\"check\": \"mate\"}"));
        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    public void customJsonProviderClient() {
        final String url = String.format("http://localhost:%d/mapper", RULE.getLocalPort());
        final String response = client.target(url)
            .request()
            .post(Entity.json(ImmutableMap.of("check", "mate")), String.class);
        assertThat(response).isEqualTo("/** A Dropwizard specialty */\n" +
            "{\"check\":\"mate\",\"hello\":\"world\"}");
    }

    @Test
    public void customJsonProviderRoundtrip() {
        final String url = String.format("http://localhost:%d/mapper", RULE.getLocalPort());
        final GenericType<Map<String, String>> typ = new GenericType<Map<String, String>>() {
        };

        final Map<String, String> response = client.target(url)
            .request()
            .post(Entity.json(ImmutableMap.of("check", "mate")), typ);
        assertThat(response).containsExactly(entry("check", "mate"), entry("hello", "world"));
    }

    @Test
    public void customBodyWriterTest() {
        final String url = String.format("http://localhost:%d/custom-class", RULE.getLocalPort());
        final String response = client.target(url)
            .request()
            .get(String.class);
        assertThat(response).isEqualTo("I'm a custom class");
    }
}
