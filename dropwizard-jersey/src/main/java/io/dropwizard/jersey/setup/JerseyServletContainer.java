package io.dropwizard.jersey.setup;

import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 * Extends {@link ServletContainer} to provide consumers of dropwizard-jersey
 * a means of obtaining a container without directly depending on Jersey.
 */
public class JerseyServletContainer extends ServletContainer {

    private static final long serialVersionUID = -3747494819983708680L;

    /**
     * Create Jersey Servlet container.
     */
    public JerseyServletContainer() {
    }

    /**
     * Create Jersey Servlet container.
     * @param resourceConfig container configuration.
     */
    public JerseyServletContainer(DropwizardResourceConfig resourceConfig) {
        super(resourceConfig);
    }
}
