package io.dropwizard.testing;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.server.DefaultServerFactory;
import org.jspecify.annotations.Nullable;

/**
 * Override port configuration setting for application and admin connectors and choose a random port.
 *
 * @see io.dropwizard.jetty.HttpConnectorFactory#getPort()
 * @see Configuration#getServerFactory()
 * @see DefaultServerFactory#getApplicationConnectors()
 * @see DefaultServerFactory#getAdminConnectors()
 */
public class ConfigOverrideRandomPorts extends ConfigOverride {

    private static final String SERVER_APPLICATION_CONNECTORS_PORT = "server.applicationConnectors[0].port";
    private static final String SERVER_ADMIN_CONNECTORS_PORT = "server.adminConnectors[0].port";

    private final String propertyPrefix;

    @Nullable
    private String applicationConnectorsPort = null;
    @Nullable
    private String adminConnectorsPort = null;

    ConfigOverrideRandomPorts(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
    }

    @Override
    public void addToSystemProperties() {
        applicationConnectorsPort = System.setProperty(propertyPrefix + SERVER_APPLICATION_CONNECTORS_PORT, "0");
        adminConnectorsPort = System.setProperty(propertyPrefix + SERVER_ADMIN_CONNECTORS_PORT, "0");
    }

    @Override
    public void removeFromSystemProperties() {
        if (applicationConnectorsPort != null) {
            System.setProperty(propertyPrefix + SERVER_APPLICATION_CONNECTORS_PORT, applicationConnectorsPort);
        } else {
            System.clearProperty(propertyPrefix + SERVER_APPLICATION_CONNECTORS_PORT);
        }

        if (adminConnectorsPort != null) {
            System.setProperty(propertyPrefix + SERVER_ADMIN_CONNECTORS_PORT, adminConnectorsPort);
        } else {
            System.clearProperty(propertyPrefix + SERVER_ADMIN_CONNECTORS_PORT);
        }
    }
}
