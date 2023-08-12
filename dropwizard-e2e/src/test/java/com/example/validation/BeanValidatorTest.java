package com.example.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Configuration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.glassfish.jersey.client.ClientProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class BeanValidatorTest {
    public static final DropwizardAppExtension<Configuration> APP = new DropwizardAppExtension<>(
        DefaultValidatorApp.class, "app1/config.yml", new ResourceConfigurationSourceProvider());

    private final ObjectMapper mapper = Jackson.newMinimalObjectMapper();
    private WebTarget target;

    @BeforeEach
    void setUp() {
        UriBuilder uriBuilder = UriBuilder.fromPath("/bean-validation")
            .scheme("http")
            .host("localhost")
            .port(APP.getLocalPort());
        target = APP.client().property(ClientProperties.CONNECT_TIMEOUT, 0).target(uriBuilder);
    }

    @Test
    void shouldValidateNormally() {
        Entity<Map<String, Object>> entity = requestBody("My string", 5, Arrays.asList("one", "two", "three"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    void shouldFailWithNullRequestBody() throws IOException {
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(null);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();

        assertThatResponseBody(response).containsOnly("The request body must not be null");
    }

    @Test
    void shouldFailWithNullString() throws IOException {
        Entity<Map<String, Object>> entity = requestBody(null, 5, Arrays.asList("one", "two", "three"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();

        assertThatResponseBody(response).containsOnly("string must not be blank");
    }

    @Test
    void shouldFailWithNegativeNumber() throws IOException {
        Entity<Map<String, Object>> entity = requestBody("My string", -23, Arrays.asList("one", "two", "three"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();
        assertThatResponseBody(response).containsOnly("number must be greater than or equal to 0");
    }

    @Test
    void shouldFailWithEmptyList() throws IOException {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("string", "My string");
        requestBody.put("number", 5);
        requestBody.put("list", Collections.emptyList());

        Entity<Map<String, Object>> entity = Entity.json(requestBody);
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();
        assertThatResponseBody(response).containsOnly("list size must be between 1 and 3");
    }

    @Test
    void shouldFailWithTooLongList() throws IOException {
        Entity<Map<String, Object>> entity = requestBody("My string", 5, Arrays.asList("one", "two", "three", "four"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();
        assertThatResponseBody(response).containsOnly("list size must be between 1 and 3");
    }

    @Test
    void shouldFailWithBlankStringInList() throws IOException {
        Entity<Map<String, Object>> entity = requestBody("My string", 5, Arrays.asList("one", " "));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();
        assertThatResponseBody(response).containsOnly("list[].<iterable element> must not be blank");
    }

    @Test
    void shouldFailWithAllInvalid() throws IOException {
        Entity<Map<String, Object>> entity = requestBody(null, -23, Arrays.asList("one", " ", "three", "four"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();
        assertThatResponseBody(response).containsOnly(
            "string must not be blank",
            "number must be greater than or equal to 0",
            "list size must be between 1 and 3",
            "list[].<iterable element> must not be blank");
    }

    @NotNull
    private Entity<Map<String, Object>> requestBody(String string, long number, List<String> list) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("string", string);
        requestBody.put("number", number);
        requestBody.put("list", list);
        return Entity.json(requestBody);
    }

    private AbstractListAssert<?, List<? extends String>, String, ObjectAssert<String>> assertThatResponseBody(Response response) throws IOException {
        return assertThat(responseBody(response).elements())
            .toIterable()
            .extracting(JsonNode::asText);
    }

    private JsonNode responseBody(Response response) throws IOException {
        InputStream responseBodyStream = (InputStream) response.getEntity();
        return mapper.readTree(responseBodyStream).withArray("errors");
    }

}
