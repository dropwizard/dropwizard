package io.dropwizard.jersey;

import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;

/**
 * Extension of {@link JerseyTest} which provides commons features for tests of the `dropwizard-jersey` module.
 */
public abstract class AbstractJerseyTest extends JerseyTest {

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 1000;
    private static final int DEFAULT_READ_TIMEOUT_MS = 5000;

    static {
        BootstrapLogging.bootstrap();
    }

    protected AbstractJerseyTest() {
        super();
        forceSet(TestProperties.CONTAINER_PORT, "0");
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.property(ClientProperties.CONNECT_TIMEOUT, DEFAULT_CONNECT_TIMEOUT_MS)
            .property(ClientProperties.READ_TIMEOUT, DEFAULT_READ_TIMEOUT_MS);
    }
}
