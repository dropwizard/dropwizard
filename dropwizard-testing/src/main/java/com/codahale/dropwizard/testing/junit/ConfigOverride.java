package com.codahale.dropwizard.testing.junit;

public class ConfigOverride {

    private final String key;
    private final String value;

    private ConfigOverride(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static ConfigOverride config(String key, String value) {
        return new ConfigOverride(key, value);
    }

    public void addToSystemProperties() {
        System.setProperty("dw." + key, value);
    }
}
