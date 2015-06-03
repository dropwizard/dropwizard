package io.dropwizard.jersey.jackson;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final ObjectMapper mapper = new ObjectMapper();
        final JacksonMessageBodyProvider provider = new JacksonMessageBodyProvider(mapper, validator);
        config.register(provider);
    }

    @Test
    public void returnsA500ForNonDeserializableRepresentationClasses() throws Exception {
        Response response = target("/json/broken").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new BrokenRepresentation(ImmutableList.of("whee")), MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    public void returnsA400ForNonDeserializableRequestEntities() throws Exception {
        Response response = target("/json/ok").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new UnknownRepresentation(100), MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(400);

        JsonNode errorMessage = response.readEntity(JsonNode.class);
        assertThat(errorMessage.get("code").asInt()).isEqualTo(400);
        assertThat(errorMessage.get("message").asText()).isEqualTo("Unable to process JSON");
        assertThat(errorMessage.has("details")).isFalse();
    }
}
