package io.dropwizard.testing.junit;

import io.dropwizard.*;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.*;

import java.net.*;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import com.codahale.metrics.health.HealthCheck;

/**
 * Test your http client code by writing a JAX-RS test double class and let this rule start and stop a Dropwizard application containing your double(s).
 * <p/>
 * Example:
 * <pre><code>    @Path("/ping")
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
 * <p/>
 * The {@link DropwizardClientRule} takes care of:
 * <ul>
 * <li>Crating a simple default configuration.</li>
 * <li>Crating a simplistic application.</li>
 * <li>Adding a dummy health check to the application (so the warning goes away... it's of no use in this case).</li>
 * <li>Adding your resources to the application.</li>
 * <li>Choosing a free port number.</li>
 * <li>Starting the application.</li>
 * <li>Stopping the application.</li>
 * </ul>
 */
public class DropwizardClientRule implements TestRule {
    private final Object[] resources;
    private final DropwizardAppRule<Configuration> appRule;

    private static class DummyHealthCheck extends HealthCheck {
        @Override
        protected Result check() {
            return Result.healthy();
        }
    }

    private class FakeApplication extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {}

        @Override
        public void run(Configuration configuration, Environment environment) {
            SimpleServerFactory serverConfig = new SimpleServerFactory();
            configuration.setServerFactory(serverConfig);
            HttpConnectorFactory connectorConfig = (HttpConnectorFactory) serverConfig.getConnector();
            connectorConfig.setPort(0);

            environment.healthChecks().register("dummy", new DummyHealthCheck());

            for (Object resource : resources) {
                environment.jersey().register(resource);
            }
        }
    }

    public DropwizardClientRule(Object... resources) {
        appRule = new DropwizardAppRule<Configuration>(null, null) {
            @Override
            public Application<Configuration> newApplication() {
                return new FakeApplication();
            }
        };
        this.resources = resources;
    }

    public URI baseUri() {
        return URI.create("http://localhost:" + appRule.getLocalPort() + "/application");
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return appRule.apply(base, description);
    }
}
