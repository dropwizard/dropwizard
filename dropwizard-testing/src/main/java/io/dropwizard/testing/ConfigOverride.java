package io.dropwizard.testing;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class ConfigOverride {

    public static final String DEFAULT_PREFIX = "dw.";
    private final String key;
    private final Supplier<String> value;
    private final String propertyPrefix;

    private ConfigOverride(String propertyPrefix, String key, Supplier<String> value) {
        this.key = key;
        this.value = value;
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
    }

    public static ConfigOverride config(String key, String value) {
        return new ConfigOverride(DEFAULT_PREFIX, key, Suppliers.ofInstance(value));
    }

    public static ConfigOverride config(String propertyPrefix, String key, String value) {
        return new ConfigOverride(propertyPrefix, key, Suppliers.ofInstance(value));
    }

    public static ConfigOverride config(String key, Supplier<String> value) {
        return new ConfigOverride(DEFAULT_PREFIX, key, value);
    }

    public static ConfigOverride config(String propertyPrefix, String key, Supplier<String> value) {
        return new ConfigOverride(propertyPrefix, key, value);
    }

    public void addToSystemProperties() {
        System.setProperty(propertyPrefix + key, value.get());
    }

    public void removeFromSystemProperties() {
        System.clearProperty(propertyPrefix + key);
    }
}
