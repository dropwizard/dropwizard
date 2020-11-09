package io.dropwizard.testing;

public class ConfigOverrideRandomPorts extends ConfigOverride {

    private static final String SERVER_APPLICATION_CONNECTORS_PORT = "server.applicationConnectors[0].port";
    private static final String SERVER_ADMIN_CONNECTORS_PORT = "server.adminConnectors[0].port";

    private final String propertyPrefix;

    ConfigOverrideRandomPorts(String propertyPrefix) {
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
    }

    public void addToSystemProperties() {
        System.setProperty(propertyPrefix + SERVER_APPLICATION_CONNECTORS_PORT, "0");
        System.setProperty(propertyPrefix + SERVER_ADMIN_CONNECTORS_PORT, "0");
    }

    public void removeFromSystemProperties() {
        System.clearProperty(propertyPrefix + SERVER_APPLICATION_CONNECTORS_PORT);
        System.clearProperty(propertyPrefix + SERVER_ADMIN_CONNECTORS_PORT);
    }
}
