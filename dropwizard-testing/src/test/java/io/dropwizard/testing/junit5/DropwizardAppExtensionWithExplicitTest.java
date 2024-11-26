package io.dropwizard.testing.junit5;

import io.dropwizard.core.Application;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.testing.app.TestConfiguration;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardAppExtensionWithExplicitTest {

    private static final DropwizardAppExtension<TestConfiguration> EXTENSION;

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
    void runWithExplicitConfig() {
        Map<?, ?> response = EXTENSION.client().target("http://localhost:" + EXTENSION.getLocalPort() + "/test")
                .request()
                .get(Map.class);
        assertThat(response).isEqualTo(Collections.singletonMap("message", "stuff!"));
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

        TestResource(String m) {
            message = m;
        }

        @GET
        public Response get() {
            return Response.ok(singletonMap("message", message)).build();
        }
    }
}
