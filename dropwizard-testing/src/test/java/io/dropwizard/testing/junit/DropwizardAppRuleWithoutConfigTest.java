package io.dropwizard.testing.junit;

import com.google.common.collect.ImmutableMap;
import io.dropwizard.HttpApplication;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.setup.HttpEnvironment;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

public class DropwizardAppRuleWithoutConfigTest {

    @ClassRule
    public static final DropwizardAppRule<HttpConfiguration> RULE = new DropwizardAppRule<>(TestApplication.class);

    Client client = ClientBuilder.newClient();

    @Test
    public void runWithoutConfigFile() {
        Map<?,?> response = client.target("http://localhost:" + RULE.getLocalPort() + "/test")
                .request()
                .get(Map.class);
        Assert.assertEquals(ImmutableMap.of("color", "orange"), response);
    }

    public static class TestApplication extends HttpApplication<HttpConfiguration> {
        @Override
        public void run(HttpConfiguration configuration, HttpEnvironment environment) throws Exception {
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
