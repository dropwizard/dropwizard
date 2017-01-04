package io.dropwizard.jersey.jackson;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.Closeable;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonProcessingExceptionMapperTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .packages("io.dropwizard.jersey.jackson");
    }

    @Override
    protected void configureClient(ClientConfig config) {
        final ObjectMapper mapper = new ObjectMapper();
        final JacksonMessageBodyProvider provider = new JacksonMessageBodyProvider(mapper);
        config.register(provider);
    }

    @Test
    public void returnsA500ForNonDeserializableRepresentationClasses() throws Exception {
        Response response = target("/json/broken").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new BrokenRepresentation(ImmutableList.of("whee")), MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void returnsA500ForListNonDeserializableRepresentationClasses() throws Exception {
        final ImmutableList<BrokenRepresentation> ent =
            ImmutableList.of(new BrokenRepresentation(ImmutableList.of()),
                new BrokenRepresentation(ImmutableList.of("whoo")));

        Response response = target("/json/brokenList").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(ent, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void returnsA500ForNonSerializableRepresentationClassesOutbound() throws Exception {
        Response response = target("/json/brokenOutbound").request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void returnsA500ForAbstractEntity() throws Exception {
        Response response = target("/json/interface").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity("\"hello\"", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void returnsA500ForAbstractEntities() throws Exception {
        Response response = target("/json/interfaceList").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity("[\"hello\"]", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.getMediaType()).isEqualTo(MediaType.APPLICATION_JSON_TYPE);
    }

    @Test
    public void returnsA400ForMalformedInputCausingIoException() throws Exception {
        assertEndpointReturns400("url", "\"no-scheme.com\"");
    }

    @Test
    public void returnsA400ForListWrongInputType() throws Exception {
        assertEndpointReturns400("urlList", "\"no-scheme.com\"");
    }

    @Test
    public void returnsA400ForMalformedListInputCausingIoException() throws Exception {
        assertEndpointReturns400("urlList", "[\"no-scheme.com\"]");
    }

    @Test
    public void returnsA400ForNonDeserializableRequestEntities() throws Exception {
        assertEndpointReturns400("ok", new UnknownRepresentation(100));
    }

    @Test
    public void returnsA400ForWrongInputType() throws Exception {
        assertEndpointReturns400("ok", "false");
    }

    @Test
    public void returnsA400ForInvalidFormatRequestEntities() throws Exception {
        assertEndpointReturns400("ok", "{\"message\": \"a\", \"date\": \"2016-01-01\"}");
    }

    @Test
    public void returnsA400ForInvalidFormatRequestEntitiesWrapped() throws Exception {
        assertEndpointReturns400("ok", "{\"message\": \"1\", \"date\": \"a\"}");
    }

    @Test
    public void returnsA400ForInvalidFormatRequestEntitiesArray() throws Exception {
        assertEndpointReturns400("ok", "{\"message\": \"1\", \"date\": [1,1,1,1]}");
    }

    @Test
    public void returnsA400ForSemanticInvalidDate() throws Exception {
        assertEndpointReturns400("ok", "{\"message\": \"1\", \"date\": [-1,-1,-1]}");
    }

    @Test
    public void handlesNullPointerExceptionWithouthThrowingNullPointerExceptions() {
        JsonProcessingExceptionMapper mapper = new JsonProcessingExceptionMapper();
        String undereferenceable = null;
        NullPointerException npeJustLikeTheVmMakesIt = null;
        try {
            undereferenceable.trim();
        } catch (NullPointerException npe) {
            npeJustLikeTheVmMakesIt = npe;
        }
        JsonProcessingException oops = null;
        Closeable bogusCloseable = new Closeable() {
            @Override
            public void close() throws IOException {
            }
        };

        try {
            oops = new JsonMappingException(bogusCloseable, "These things actually happen", npeJustLikeTheVmMakesIt) {};
        } catch (NullPointerException e) {
            Assert.fail(mapper + " should not throw a NullPointerException: " + e);
        }
        Assert.assertNotNull(mapper + " should return a response",
            mapper.toResponse(oops));
    }

    private <T> void assertEndpointReturns400(String endpoint, T entity) {
        Response response = target(String.format("/json/%s", endpoint))
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(entity, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(400);

        JsonNode errorMessage = response.readEntity(JsonNode.class);
        assertThat(errorMessage.get("code").asInt()).isEqualTo(400);
        assertThat(errorMessage.get("message").asText()).isEqualTo("Unable to process JSON");
        assertThat(errorMessage.has("details")).isFalse();
    }
}
