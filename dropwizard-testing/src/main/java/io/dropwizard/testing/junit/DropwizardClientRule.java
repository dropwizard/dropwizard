package io.dropwizard.testing.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.common.DropwizardClient;
import org.junit.rules.ExternalResource;

import java.net.URI;
import java.net.URL;

//@formatter:off
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
 *
 * @deprecated Deprecated since Dropwizard 2.0.0. Please migrate to JUnit 5 and {@link io.dropwizard.testing.junit5.DropwizardClientExtension}.
 */
//@formatter:off
@Deprecated
public class DropwizardClientRule extends ExternalResource {
    private final DropwizardClient client;

    public DropwizardClientRule(Object... resources) {
        this.client = new DropwizardClient(resources);
    }

    public URI baseUri() {
        return client.baseUri();
    }

    public ObjectMapper getObjectMapper() {
        return client.getObjectMapper();
    }

    public Environment getEnvironment() {
        return client.getEnvironment();
    }

    @Override
    protected void before() throws Throwable {
        client.before();
    }

    @Override
    protected void after() {
        client.after();
    }
}
