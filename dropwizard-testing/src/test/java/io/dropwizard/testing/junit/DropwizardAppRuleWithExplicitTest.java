package io.dropwizard.testing.junit;

import io.dropwizard.Application;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
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

import com.google.common.collect.ImmutableMap;

public class DropwizardAppRuleWithExplicitTest {

    @Test
    public void bogusTest() { }

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE;
    static {
        // Bit complicated, as we want to avoid using the default http port (8080)
        // as there is another test that uses it already. So force bogus value of
        // 0, similar to what `test-config.yaml` defines.
        TestConfiguration config = new TestConfiguration("stuff!", "extra");
        DefaultServerFactory sf = (DefaultServerFactory) config.getServerFactory();
        ((HttpConnectorFactory) sf.getApplicationConnectors().get(0)).setPort(0);
        ((HttpConnectorFactory) sf.getAdminConnectors().get(0)).setPort(0);
        RULE = new DropwizardAppRule<>(TestApplication.class, config);
    }

    Client client = ClientBuilder.newClient();

    @Test
    public void runWithExplicitConfig() {
        Map<?,?> response = client.target("http://localhost:" + RULE.getLocalPort() + "/test")
                .request()
                .get(Map.class);
        Assert.assertEquals(ImmutableMap.of("message", "stuff!"), response);
    }

    public static class TestApplication extends Application<TestConfiguration> {
        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
            environment.jersey().register(new TestResource(configuration.getMessage()));
        }
    }

    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public static class TestResource {
        private final String message;

        public TestResource(String m) { message = m; }

        @GET
        public Response get() {
            return Response.ok(ImmutableMap.of("message", message)).build();
        }
    }
}
