package io.dropwizard.jersey.setup;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Extends {@link ServletContainer} to provide consumers of dropwizard-jersey a means of obtaining a ServletContainer
 * without directly depending on Jersey.
 */
public class JerseyServletContainer extends ServletContainer {

    private static final long serialVersionUID = 8217023192775181696L;

    /**
     * Create Jersey Servlet container.
     */
    public JerseyServletContainer() {
    }

    /**
     * Create Jersey Servlet container.
     * @param resourceConfig container configuration.
     */
    public JerseyServletContainer(final ResourceConfig resourceConfig) {
        super(resourceConfig);
    }
}
