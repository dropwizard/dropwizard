package io.dropwizard.testing.junit;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class DropwizardAppRuleWithoutConfigTest {

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(TestApplication.class, null,
        ConfigOverride.config("server.applicationConnectors[0].port", "0"),
        ConfigOverride.config("server.adminConnectors[0].port", "0"));

    @Test
    public void runWithoutConfigFile() {
        Map<String, String> response = RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/test")
            .request()
            .get(new GenericType<Map<String, String>>() {
            });
        assertThat(response).containsOnly(entry("color", "orange"));
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
