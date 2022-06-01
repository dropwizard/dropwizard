package io.dropwizard.jersey.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.jupiter.api.Test;

abstract class JsonProcessingExceptionMapperTest extends AbstractJerseyTest {

    abstract boolean showDetails();

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .packages("io.dropwizard.jersey.jackson")
                .register(new JsonProcessingExceptionMapper(showDetails()))
                .register(new LoggingExceptionMapper<Throwable>() {});
    }

    @Override
    protected void configureClient(ClientConfig config) {
        final ObjectMapper mapper = new ObjectMapper();
        final JacksonMessageBodyProvider provider = new JacksonMessageBodyProvider(mapper);
        config.register(provider);
    }

    @Test
    void returnsA500ForNonDeserializableRepresentationClasses() throws Exception {
        assertEndpointReturns500("broken", new BrokenRepresentation(Collections.singletonList("whee")));
    }

    @Test
    void returnsA500ForListNonDeserializableRepresentationClasses() throws Exception {
        final List<BrokenRepresentation> ent = Arrays.asList(
                new BrokenRepresentation(Collections.emptyList()),
                new BrokenRepresentation(Collections.singletonList("whoo")));

        assertEndpointReturns500("brokenList", ent);
    }

    @Test
    void returnsA500ForNonSerializableRepresentationClassesOutbound() throws Exception {
        Response response = target("/json/brokenOutbound")
                .request(MediaType.APPLICATION_JSON)
                .get();
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    void returnsA500ForAbstractEntity() throws Exception {
        assertEndpointReturns500("interface", "\"hello\"");
    }

    @Test
    void returnsA500ForAbstractEntities() throws Exception {
        assertEndpointReturns500("interfaceList", "[\"hello\"]");
    }

    @Test
    void returnsA400ForCustomDeserializer() throws Exception {
        assertEndpointReturns400WithoutDetails("custom", "{}");
    }

    @Test
    void returnsA500ForCustomDeserializerUnexpected() throws Exception {
        assertEndpointReturns500("custom", "\"SQL_INECTION\"");
    }

    @Test
    void returnsA400ForMalformedInputCausingIoException() throws Exception {
        assertEndpointReturns400("url", "\"no-scheme.com\"");
    }

    @Test
    void returnsA400ForListWrongInputType() throws Exception {
        assertEndpointReturns400("urlList", "\"no-scheme.com\"");
    }

    @Test
    void returnsA400ForMalformedListInputCausingIoException() throws Exception {
        assertEndpointReturns400("urlList", "[\"no-scheme.com\"]");
    }

    @Test
    void returnsA400ForWrongInputType() throws Exception {
        assertEndpointReturns400("ok", "false");
    }

    @Test
    void returnsA400ForInvalidFormatRequestEntities() throws Exception {
        assertEndpointReturns400("ok", "{\"message\": \"a\", \"date\": \"2016-01-01\"}");
    }

    @Test
    void returnsA400ForInvalidFormatRequestEntitiesWrapped() throws Exception {
        assertEndpointReturns400("ok", "{\"message\": \"1\", \"date\": \"a\"}");
    }

    @Test
    void returnsA400ForInvalidFormatRequestEntitiesArray() throws Exception {
        assertEndpointReturns400("ok", "{\"message\": \"1\", \"date\": [1,1,1,1]}");
    }

    @Test
    void returnsA400ForSemanticInvalidDate() throws Exception {
        assertEndpointReturns400("ok", "{\"message\": \"1\", \"date\": [-1,-1,-1]}");
    }

    private <T> void assertEndpointReturns400WithoutDetails(String endpoint, T entity) {
        Response response = target(String.format("/json/%s", endpoint))
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(entity, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(400);

        JsonNode errorMessage = response.readEntity(JsonNode.class);
        assertThat(errorMessage.path("code").asInt()).isEqualTo(400);
        assertThat(errorMessage.path("message").asText()).isEqualTo("Unable to process JSON");
        assertThat(errorMessage.has("details")).isFalse();
    }

    private <T> void assertEndpointReturns400(String endpoint, T entity) {
        Response response = target(String.format("/json/%s", endpoint))
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(entity, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(400);

        JsonNode errorMessage = response.readEntity(JsonNode.class);
        assertThat(errorMessage.path("code").asInt()).isEqualTo(400);
        assertThat(errorMessage.path("message").asText()).isEqualTo("Unable to process JSON");
        assertThat(errorMessage.has("details")).isEqualTo(showDetails());

        if (showDetails()) {
            assertThat(errorMessage.get("details").asText()).isNotBlank();
        }
    }

    private <T> void assertEndpointReturns500(String endpoint, T entity) {
        Response response = target(String.format("/json/%s", endpoint))
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(entity, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);

        JsonNode errorMessage = response.readEntity(JsonNode.class);
        assertThat(errorMessage.path("code").asInt()).isEqualTo(500);
        assertThat(errorMessage.path("message").asText())
                .matches("There was an error processing your request. It has been logged \\(ID \\w+\\).");
        assertThat(errorMessage.has("details")).isFalse();
    }
}
