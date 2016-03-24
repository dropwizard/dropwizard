package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.dropwizard.jackson.Jackson;
import org.glassfish.jersey.filter.LoggingFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_PLAIN;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test of {@link org.glassfish.jersey.client.JerseyClient}
 * with {@link io.dropwizard.client.DropwizardApacheConnector}
 */
public class JerseyClientIntegrationTest {

    private static final String TRANSFER_ENCODING = "Transfer-Encoding";
    private static final String CHUNKED = "chunked";
    private static final String GZIP = "gzip";
    private static final ObjectMapper JSON_MAPPER = Jackson.newObjectMapper();
    private static final String GZIP_DEFLATE = "gzip,deflate";
    private static final String JSON_TOKEN = JSON_MAPPER.createObjectNode()
            .put("id", 214)
            .put("token", "a23f78bc31cc5de821ad9412e")
            .toString();

    private HttpServer httpServer;

    @Before
    public void setup() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
    }

    @After
    public void tearDown() throws Exception {
        httpServer.stop(0);
    }

    @Test
    public void testChunkedGzipPost() throws Exception {
        httpServer.createContext("/register", httpExchange -> {
            try {
                Headers requestHeaders = httpExchange.getRequestHeaders();
                assertThat(requestHeaders.get(TRANSFER_ENCODING)).containsExactly(CHUNKED);
                assertThat(requestHeaders.get(HttpHeaders.CONTENT_LENGTH)).isNull();
                assertThat(requestHeaders.get(HttpHeaders.CONTENT_ENCODING)).containsExactly(GZIP);
                assertThat(requestHeaders.get(HttpHeaders.ACCEPT_ENCODING)).containsExactly(GZIP_DEFLATE);
                checkBody(httpExchange, true);
                postResponse(httpExchange);
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();

        postRequest(new JerseyClientConfiguration());
    }

    @Test
    public void testBufferedGzipPost() {
        httpServer.createContext("/register", httpExchange -> {
            try {
                Headers requestHeaders = httpExchange.getRequestHeaders();

                assertThat(requestHeaders.get(HttpHeaders.CONTENT_LENGTH)).containsExactly("58");
                assertThat(requestHeaders.get(TRANSFER_ENCODING)).isNull();
                assertThat(requestHeaders.get(HttpHeaders.CONTENT_ENCODING)).containsExactly(GZIP);
                assertThat(requestHeaders.get(HttpHeaders.ACCEPT_ENCODING));

                checkBody(httpExchange, true);
                postResponse(httpExchange);
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();

        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setChunkedEncodingEnabled(false);
        postRequest(configuration);
    }

    @Test
    public void testChunkedPost() throws Exception {
        httpServer.createContext("/register", httpExchange -> {
            try {
                Headers requestHeaders = httpExchange.getRequestHeaders();
                assertThat(requestHeaders.get(TRANSFER_ENCODING)).containsExactly(CHUNKED);
                assertThat(requestHeaders.get(HttpHeaders.CONTENT_LENGTH)).isNull();
                assertThat(requestHeaders.get(HttpHeaders.CONTENT_ENCODING)).isNull();
                assertThat(requestHeaders.get(HttpHeaders.ACCEPT_ENCODING)).containsExactly(GZIP_DEFLATE);

                checkBody(httpExchange, false);
                postResponse(httpExchange);
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();

        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setGzipEnabledForRequests(false);
        postRequest(configuration);
    }

    @Test
    public void testChunkedPostWithoutGzip() throws Exception {
        httpServer.createContext("/register", httpExchange -> {
            try {
                Headers requestHeaders = httpExchange.getRequestHeaders();
                assertThat(requestHeaders.get(TRANSFER_ENCODING)).containsExactly(CHUNKED);
                assertThat(requestHeaders.get(HttpHeaders.CONTENT_LENGTH)).isNull();
                assertThat(requestHeaders.get(HttpHeaders.CONTENT_ENCODING)).isNull();
                assertThat(requestHeaders.get(HttpHeaders.ACCEPT_ENCODING)).isNull();

                checkBody(httpExchange, false);

                httpExchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
                httpExchange.sendResponseHeaders(200, 0);
                httpExchange.getResponseBody().write(JSON_TOKEN.getBytes(StandardCharsets.UTF_8));
                httpExchange.getResponseBody().close();
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();

        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setGzipEnabled(false);
        configuration.setGzipEnabledForRequests(false);
        postRequest(configuration);
    }

    private void postRequest(JerseyClientConfiguration configuration) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Client jersey = new JerseyClientBuilder(new MetricRegistry())
                .using(executor, JSON_MAPPER)
                .using(configuration)
                .build("jersey-test");
        Response response = jersey.target("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/register")
                .request()
                .buildPost(Entity.entity(new Person("john@doe.me", "John Doe"), APPLICATION_JSON))
                .invoke();

        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(APPLICATION_JSON);
        assertThat(response.getHeaderString(TRANSFER_ENCODING)).isEqualTo(CHUNKED);

        Credentials credentials = response.readEntity(Credentials.class);
        assertThat(credentials.id).isEqualTo(214);
        assertThat(credentials.token).isEqualTo("a23f78bc31cc5de821ad9412e");

        executor.shutdown();
        jersey.close();
    }

    private void postResponse(HttpExchange httpExchange) throws IOException {
        httpExchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
        httpExchange.getResponseHeaders().add(HttpHeaders.CONTENT_ENCODING, GZIP);
        httpExchange.sendResponseHeaders(200, 0);
        GZIPOutputStream gzipStream = new GZIPOutputStream(httpExchange.getResponseBody());
        gzipStream.write(JSON_TOKEN.getBytes(StandardCharsets.UTF_8));
        gzipStream.close();
    }


    private void checkBody(HttpExchange httpExchange, boolean gzip) throws IOException {
        assertThat(httpExchange.getRequestHeaders().get(HttpHeaders.CONTENT_TYPE))
                .containsExactly(APPLICATION_JSON);

        InputStream requestBody = gzip ? new GZIPInputStream(httpExchange.getRequestBody()) :
                httpExchange.getRequestBody();
        String body = CharStreams.toString(new InputStreamReader(requestBody, StandardCharsets.UTF_8));
        assertThat(JSON_MAPPER.readTree(body)).isEqualTo(JSON_MAPPER.createObjectNode()
                .put("email", "john@doe.me")
                .put("name", "John Doe"));
    }


    @Test
    public void testGet() {
        httpServer.createContext("/player", httpExchange -> {
            try {
                assertThat(httpExchange.getRequestURI().getQuery()).isEqualTo("id=21");

                httpExchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON);
                httpExchange.sendResponseHeaders(200, 0);
                httpExchange.getResponseBody().write(JSON_MAPPER.createObjectNode()
                        .put("email", "john@doe.me")
                        .put("name", "John Doe")
                        .toString().getBytes(StandardCharsets.UTF_8));
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Client jersey = new JerseyClientBuilder(new MetricRegistry())
                .using(executor, JSON_MAPPER)
                .using(new JerseyClientConfiguration())
                .build("jersey-test");
        Response response = jersey.target("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/player?id=21")
                .request()
                .buildGet()
                .invoke();

        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(APPLICATION_JSON);
        assertThat(response.getHeaderString(TRANSFER_ENCODING)).isEqualTo(CHUNKED);

        Person person = response.readEntity(Person.class);
        assertThat(person.email).isEqualTo("john@doe.me");
        assertThat(person.name).isEqualTo("John Doe");

        executor.shutdown();
        jersey.close();
    }

    @Test
    public void testSetUserAgent() {
        httpServer.createContext("/test", httpExchange -> {
            try {
                assertThat(httpExchange.getRequestHeaders().get(HttpHeaders.USER_AGENT))
                        .containsExactly("Custom user-agent");
                httpExchange.sendResponseHeaders(200, 0);
                httpExchange.getResponseBody().write("Hello World!".getBytes(StandardCharsets.UTF_8));
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();

        JerseyClientConfiguration configuration = new JerseyClientConfiguration();
        configuration.setUserAgent(Optional.of("Custom user-agent"));

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Client jersey = new JerseyClientBuilder(new MetricRegistry())
                .using(executor, JSON_MAPPER)
                .using(configuration)
                .build("jersey-test");
        String text = jersey.target("http://127.0.0.1:" + httpServer.getAddress().getPort() + "/test")
                .request()
                .buildGet()
                .invoke()
                .readEntity(String.class);
        assertThat(text).isEqualTo("Hello World!");

        executor.shutdown();
        jersey.close();
    }

    /**
     * Test for ConnectorProvider idempotency
     */
    @Test
    public void testFilterOnAWebTarget() {
        httpServer.createContext("/test", httpExchange -> {
            try {
                httpExchange.getResponseHeaders().add(HttpHeaders.CONTENT_TYPE, TEXT_PLAIN);
                httpExchange.sendResponseHeaders(200, 0);
                httpExchange.getResponseBody().write("Hello World!".getBytes(StandardCharsets.UTF_8));
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Client jersey = new JerseyClientBuilder(new MetricRegistry())
                .using(executor, JSON_MAPPER)
                .build("test-jersey-client");
        String uri = "http://127.0.0.1:" + httpServer.getAddress().getPort() + "/test";

        WebTarget target = jersey.target(uri);
        target.register(new LoggingFilter());
        String firstResponse = target.request()
                .buildGet()
                .invoke()
                .readEntity(String.class);
        assertThat(firstResponse).isEqualTo("Hello World!");

        String secondResponse = jersey.target(uri)
                .request()
                .buildGet()
                .invoke()
                .readEntity(String.class);
        assertThat(secondResponse).isEqualTo("Hello World!");

        executor.shutdown();
        jersey.close();
    }

    static class Person {

        @JsonProperty("email")
        final String email;

        @JsonProperty("name")
        final String name;

        Person(@JsonProperty("email") String email, @JsonProperty("name") String name) {
            this.email = email;
            this.name = name;
        }
    }

    static class Credentials {

        @JsonProperty("id")
        final long id;

        @JsonProperty("token")
        final String token;

        Credentials(@JsonProperty("id") long id, @JsonProperty("token") String token) {
            this.id = id;
            this.token = token;
        }
    }
}
