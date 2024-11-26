package io.dropwizard.testing.junit5;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionWithoutConfigTest {

    private static final DropwizardAppExtension<Configuration> EXTENSION = new DropwizardAppExtension<>(TestApplication.class, null,
        ConfigOverride.config("server.applicationConnectors[0].port", "0"),
        ConfigOverride.config("server.adminConnectors[0].port", "0"));

    @Test
    void runWithoutConfigFile() {
        Map<?, ?> response = EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/test")
            .request()
            .get(Map.class);
        assertThat(response).isEqualTo(Collections.singletonMap("color", "orange"));
    }

    public static class TestApplication extends Application<Configuration> {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(new TestResource());
        }
    }

    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestResource {
        @GET
        public Response get() {
            return Response.ok(Collections.singletonMap("color", "orange")).build();
        }
    }
}
