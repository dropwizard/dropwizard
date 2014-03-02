package io.dropwizard.jersey.jackson;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonProcessingExceptionMapperTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected Application configure() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.packages("io.dropwizard.jersey.jackson");
        return rc;
    }

    @Test
    public void returnsA500ForNonDeserializableRepresentationClasses() throws Exception {
        Response response = target("/json/broken")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new BrokenRepresentation(ImmutableList.of("whee")),
                        MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    public void returnsA400ForNonDeserializableRequestEntities() throws Exception {
        Response response = target("/json/ok")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(new UnknownRepresentation(100),
                        MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(400);
    }
}
