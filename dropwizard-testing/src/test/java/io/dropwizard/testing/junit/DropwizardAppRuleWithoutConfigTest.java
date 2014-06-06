package io.dropwizard.testing.junit;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.Client;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class DropwizardAppRuleWithoutConfigTest {

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(TestApplication.class, null);

    Client client = new Client();

    @Test
    public void runWithoutConfigFile() {
        Map response = client.resource("http://localhost:" + RULE.getLocalPort() + "/test").get(Map.class);
        Assert.assertEquals(ImmutableMap.of("color", "orange"), response);
    }

    static class TestApplication extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
        }

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
