package com.example.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Configuration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.validation.constraints.NotNull;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class BeanValidatorTest {
    public static final DropwizardAppExtension<Configuration> APP = new DropwizardAppExtension<>(
        DefaultValidatorApp.class, resourceFilePath("app1/config.yml"));

    private final ObjectMapper mapper = Jackson.newMinimalObjectMapper();
    private WebTarget target;

    @BeforeEach
    void setUp() {
        UriBuilder uriBuilder = UriBuilder.fromPath("/bean-validation")
            .scheme("http")
            .host("localhost")
            .port(APP.getLocalPort());
        target = APP.client().target(uriBuilder);
    }

    @Test
    public void shouldValidateNormally() {
        Entity<Map<String, Object>> entity = requestBody("My string", 5, Arrays.asList("one", "two", "three"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(204);
    }

    @Test
    public void shouldFailWithNullRequestBody() throws IOException {
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(null);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();

        assertThatResponseBody(response).containsOnly("The request body must not be null");
    }

    @Test
    public void shouldFailWithNullString() throws IOException {
        Entity<Map<String, Object>> entity = requestBody(null, 5, Arrays.asList("one", "two", "three"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();

        assertThatResponseBody(response).containsOnly("string must not be blank");
    }

    @Test
    public void shouldFailWithNegativeNumber() throws IOException {
        Entity<Map<String, Object>> entity = requestBody("My string", -23, Arrays.asList("one", "two", "three"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();
        assertThatResponseBody(response).containsOnly("number must be greater than or equal to 0");
    }

    @Test
    public void shouldFailWithEmptyList() throws IOException {
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
    public void shouldFailWithTooLongList() throws IOException {
        Entity<Map<String, Object>> entity = requestBody("My string", 5, Arrays.asList("one", "two", "three", "four"));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();
        assertThatResponseBody(response).containsOnly("list size must be between 1 and 3");
    }

    @Test
    public void shouldFailWithBlankStringInList() throws IOException {
        Entity<Map<String, Object>> entity = requestBody("My string", 5, Arrays.asList("one", " "));
        Response response = target.request(MediaType.APPLICATION_JSON_TYPE).post(entity);

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.hasEntity()).isTrue();
        assertThatResponseBody(response).containsOnly("list[].<iterable element> must not be blank");
    }

    @Test
    public void shouldFailWithAllInvalid() throws IOException {
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
