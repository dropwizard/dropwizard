package io.dropwizard.testing.junit;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import java.util.Map;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

public class DropwizardAppRuleWithoutConfigTest {

    @ClassRule
    public static final DropwizardAppRule<Configuration> RULE = new DropwizardAppRule<>(TestApplication.class);

    Client client = ClientBuilder.newClient();

    @Test
    public void runWithoutConfigFile() {
        Map<?,?> response = client.target("http://localhost:" + RULE.getLocalPort() + "/test")
                .request()
                .get(Map.class);
        Assert.assertEquals(ImmutableMap.of("color", "orange"), response);
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
