package io.dropwizard.testing;

public class ConfigOverride {

    private final String key;
    private final String value;
    private final String propertyPrefix;

    private ConfigOverride(String propertyPrefix, String key, String value) {
        this.key = key;
        this.value = value;
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
    }

    public static ConfigOverride config(String key, String value) {
        return new ConfigOverride("dw.", key, value);
    }

    public static ConfigOverride config(String propertyPrefix, String key, String value) {
        return new ConfigOverride(propertyPrefix, key, value);
    }

    public void addToSystemProperties() {
        System.setProperty(propertyPrefix + key, value);
    }

    public void removeFromSystemProperties() {
        System.clearProperty(propertyPrefix + key);
    }
}
