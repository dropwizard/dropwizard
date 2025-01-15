package io.dropwizard.testing;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public class ConfigOverrideValue extends ConfigOverride {

    private final String key;
    private final Supplier<String> value;
    private final String propertyPrefix;
    @Nullable
    private String originalValue = null;

    ConfigOverrideValue(String propertyPrefix, String key, Supplier<String> value) {
        this.key = key;
        this.value = value;
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + ".";
    }

    @Override
    public void addToSystemProperties() {
        this.originalValue = System.setProperty(propertyPrefix + key, value.get());
    }

    @Override
    public void removeFromSystemProperties() {
        if (originalValue != null) {
            System.setProperty(propertyPrefix + key, originalValue);
        } else {
            System.clearProperty(propertyPrefix + key);
        }
    }
}
