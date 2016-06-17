package io.dropwizard.testing;

import java.util.function.Supplier;
import io.dropwizard.testing.junit.DropwizardAppRule;

/**
 * An override for a field in dropwizard configuration intended for use with
 * {@link DropwizardAppRule}.
 * <p>
 * Given a configuration file containing
 * <pre>
 * ---
 * server:
 *   applicationConnectors:
 *     - type: http
 *       port: 8000
 *   adminConnectors:
 *     - type: http
 *       port: 8001
 *
 * logging:
 *   loggers:
 *     com.example.foo: INFO
 * </pre>
 * <ul>
 * <li><code>ConfigOverride.config("debug", "true")</code> will add a top level
 * field named "debug" mapped to the string "true".</li>
 * <li><code>ConfigOverride.config("server.applicationConnectors[0].type",
 * "https")</code> will change the sole application connector to have type
 * "https" instead of type "http".
 * <li><code>ConfigOverride.config("logging.loggers.com\\.example\\.bar",
 * "DEBUG")</code> will add a logger with the name "com.example.bar" configured
 * for debug logging.</li>
 * </ul>
 */
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
        return new ConfigOverride(DEFAULT_PREFIX, key, () -> value);
    }

    public static ConfigOverride config(String propertyPrefix, String key, String value) {
        return new ConfigOverride(propertyPrefix, key, () -> value);
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
