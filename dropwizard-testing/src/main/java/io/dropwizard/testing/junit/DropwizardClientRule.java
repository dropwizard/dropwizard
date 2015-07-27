package io.dropwizard.testing.junit;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.HttpApplication;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.HttpEnvironment;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.rules.ExternalResource;

import java.net.URI;
import java.net.URL;

/**
 * Test your HTTP client code by writing a JAX-RS test double class and let this rule start and stop a
 * Dropwizard application containing your doubles.
 * <p>
 * Example:
 * <pre><code>
    {@literal @}Path("/ping")
    public static class PingResource {
        {@literal @}GET
        public String ping() {
            return "pong";
        }
    }

    {@literal @}ClassRule
    public static DropwizardClientRule dropwizard = new DropwizardClientRule(new PingResource());

    {@literal @}Test
    public void shouldPing() throws IOException {
        URL url = new URL(dropwizard.baseUri() + "/ping");
        String response = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
        assertEquals("pong", response);
    }
</code></pre>
 * Of course, you'd use your http client, not {@link URL#openStream()}.
 * </p>
 * <p>
 * The {@link DropwizardClientRule} takes care of:
 * <ul>
 * <li>Creating a simple default configuration.</li>
 * <li>Creating a simplistic application.</li>
 * <li>Adding a dummy health check to the application to suppress the startup warning.</li>
 * <li>Adding your resources to the application.</li>
 * <li>Choosing a free random port number.</li>
 * <li>Starting the application.</li>
 * <li>Stopping the application.</li>
 * </ul>
 * </p>
 */
public class DropwizardClientRule extends ExternalResource {
    private final Object[] resources;
    private final DropwizardTestSupport<HttpConfiguration> testSupport;

    public DropwizardClientRule(Object... resources) {
        testSupport = new DropwizardTestSupport<HttpConfiguration>(FakeApplication.class, "") {
            @Override
            public HttpApplication<HttpConfiguration> newApplication() {
                return new FakeApplication();
            }
        };
        this.resources = resources;
    }

    public URI baseUri() {
        return URI.create("http://localhost:" + testSupport.getLocalPort() + "/application");
    }

    @Override
    protected void before() throws Throwable {
        testSupport.before();
    }

    @Override
    protected void after() {
        testSupport.after();
    }

    private static class DummyHealthCheck extends HealthCheck {
        @Override
        protected Result check() {
            return Result.healthy();
        }
    }

    private class FakeApplication extends HttpApplication<HttpConfiguration> {
        @Override
        public void run(HttpConfiguration configuration, HttpEnvironment environment) {
            final SimpleServerFactory serverConfig = new SimpleServerFactory();
            configuration.setServerFactory(serverConfig);
            final HttpConnectorFactory connectorConfig = (HttpConnectorFactory) serverConfig.getConnector();
            connectorConfig.setPort(0);

            environment.healthChecks().register("dummy", new DummyHealthCheck());

            for (Object resource : resources) {
                if (resource instanceof Class<?>) {
                    environment.jersey().register((Class<?>) resource);
                } else {
                    environment.jersey().register(resource);
                }
            }
        }
    }
}
