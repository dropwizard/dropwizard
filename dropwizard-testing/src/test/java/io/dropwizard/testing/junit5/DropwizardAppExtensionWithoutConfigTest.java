package io.dropwizard.testing.junit5;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardAppExtensionWithoutConfigTest {

    public static final DropwizardAppExtension<Configuration> EXTENSION = new DropwizardAppExtension<>(TestApplication.class, null,
        ConfigOverride.config("server.applicationConnectors[0].port", "0"),
        ConfigOverride.config("server.adminConnectors[0].port", "0"));

    @Test
    public void runWithoutConfigFile() {
        Map<?, ?> response = EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/test")
            .request()
            .get(Map.class);
        Assertions.assertEquals(ImmutableMap.of("color", "orange"), response);
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
            return Response.ok(ImmutableMap.of("color", "orange")).build();
        }
    }
}
