package io.dropwizard.testing.junit5;

import io.dropwizard.Application;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardAppExtensionWithExplicitTest {

    public static final DropwizardAppExtension<TestConfiguration> EXTENSION;

    static {
        // Bit complicated, as we want to avoid using the default http port (8080)
        // as there is another test that uses it already. So force bogus value of
        // 0, similar to what `test-config.yaml` defines.
        TestConfiguration config = new TestConfiguration("stuff!", "extra");
        DefaultServerFactory sf = (DefaultServerFactory) config.getServerFactory();
        ((HttpConnectorFactory) sf.getApplicationConnectors().get(0)).setPort(0);
        ((HttpConnectorFactory) sf.getAdminConnectors().get(0)).setPort(0);
        EXTENSION = new DropwizardAppExtension<>(TestApplication.class, config);
    }


    @Test
    public void runWithExplicitConfig() {
        Map<?, ?> response = EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/test")
                .request()
                .get(Map.class);
        Assertions.assertEquals(Collections.singletonMap("message", "stuff!"), response);
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

        public TestResource(String m) {
            message = m;
        }

        @GET
        public Response get() {
            return Response.ok(Collections.singletonMap("message", message)).build();
        }
    }
}
