package io.dropwizard.testing;

import java.util.function.Supplier;

public class ConfigOverrideValue extends ConfigOverride {

    private final String key;
    private final Supplier<String> value;
    private final String propertyPrefix;

    ConfigOverrideValue(String propertyPrefix, String key, Supplier<String> value) {
        this.key = key;
        this.value = value;
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
    }

    public void addToSystemProperties() {
        System.setProperty(propertyPrefix + key, value.get());
    }

    public void removeFromSystemProperties() {
        System.clearProperty(propertyPrefix + key);
    }
}
