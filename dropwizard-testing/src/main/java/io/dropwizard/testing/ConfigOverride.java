package io.dropwizard.testing;

import java.util.function.Supplier;

/**
 * An override for a field in dropwizard configuration intended for use with
 * {@link io.dropwizard.testing.junit5.DropwizardAppExtension}.
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
 * <li><code>ConfigOverride.randomPorts()</code> will change the ports of the
 * default applicationConnectors and adminConnectors to 0 so the tests start
 * with random ports.</li>
 * </ul>
 */
public abstract class ConfigOverride {

    static final String DEFAULT_PREFIX = "dw.";

    public static ConfigOverride config(String key, String value) {
        return new ConfigOverrideValue(DEFAULT_PREFIX, key, () -> value);
    }

    public static ConfigOverride config(String propertyPrefix, String key, String value) {
        return new ConfigOverrideValue(propertyPrefix, key, () -> value);
    }

    public static ConfigOverride config(String key, Supplier<String> value) {
        return new ConfigOverrideValue(DEFAULT_PREFIX, key, value);
    }

    public static ConfigOverride config(String propertyPrefix, String key, Supplier<String> value) {
        return new ConfigOverrideValue(propertyPrefix, key, value);
    }

    public static ConfigOverride randomPorts() {
        return new ConfigOverrideRandomPorts(DEFAULT_PREFIX);
    }

    public static ConfigOverride randomPorts(String propertyPrefix) {
        return new ConfigOverrideRandomPorts(propertyPrefix);
    }

    public abstract void addToSystemProperties();

    public abstract void removeFromSystemProperties();
}
