package io.dropwizard.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.server.DefaultServerFactory;
import io.dropwizard.core.server.ServerFactory;
import io.dropwizard.core.setup.AdminFactory;
import io.dropwizard.health.HealthFactory;
import io.dropwizard.logging.common.DefaultLoggingFactory;
import io.dropwizard.logging.common.LoggingFactory;
import io.dropwizard.metrics.common.MetricsFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * An object representation of the YAML configuration file. Extend this with your own configuration
 * properties, and they'll be parsed from the YAML file as well.
 * <p/>
 * For example, given a YAML file with this:
 * <pre>
 * name: "Random Person"
 * age: 43
 * # ... etc ...
 * </pre>
 * And a configuration like this:
 * <pre>
 * public class ExampleConfiguration extends Configuration {
 *     \@NotNull
 *     private String name;
 *
 *     \@Min(1)
 *     \@Max(120)
 *     private int age;
 *
 *     \@JsonProperty
 *     public String getName() {
 *         return name;
 *     }
 *
 *     \@JsonProperty
 *     public void setName(String name) {
 *         this.name = name;
 *     }
 *
 *     \@JsonProperty
 *     public int getAge() {
 *         return age;
 *     }
 *
 *     \@JsonProperty
 *     public void setAge(int age) {
 *         this.age = age;
 *     }
 * }
 * </pre>
 * <p/>
 * Dropwizard will parse the given YAML file and provide an {@code ExampleConfiguration} instance
 * to your application whose {@code getName()} method will return {@code "Random Person"} and whose
 * {@code getAge()} method will return {@code 43}.
 *
 * @see <a href="http://www.yaml.org/YAML_for_ruby.html">YAML Cookbook</a>
 */
public class Configuration {
    @Valid
    @NotNull
    private ServerFactory server = new DefaultServerFactory();

    @Valid
    @Nullable
    private LoggingFactory logging;

    @Valid
    @NotNull
    private MetricsFactory metrics = new MetricsFactory();

    @Valid
    @NotNull
    private AdminFactory admin = new AdminFactory();

    @Valid
    @Nullable
    private HealthFactory health;

    /**
     * Returns the server-specific section of the configuration file.
     *
     * @return server-specific configuration parameters
     */
    @JsonProperty("server")
    public ServerFactory getServerFactory() {
        return server;
    }

    /**
     * Sets the HTTP-specific section of the configuration file.
     */
    @JsonProperty("server")
    public void setServerFactory(ServerFactory factory) {
        this.server = factory;
    }

    /**
     * Returns the logging-specific section of the configuration file.
     *
     * @return logging-specific configuration parameters
     */
    @JsonProperty("logging")
    public synchronized LoggingFactory getLoggingFactory() {
        if (logging == null) {
            // Lazy init to avoid a hard dependency to logback
            logging = new DefaultLoggingFactory();
        }
        return logging;
    }

    /**
     * Sets the logging-specific section of the configuration file.
     */
    @JsonProperty("logging")
    public synchronized void setLoggingFactory(LoggingFactory factory) {
        this.logging = factory;
    }

    @JsonProperty("metrics")
    public MetricsFactory getMetricsFactory() {
        return metrics;
    }

    @JsonProperty("metrics")
    public void setMetricsFactory(MetricsFactory metrics) {
        this.metrics = metrics;
    }

    /**
     * Returns the admin interface-specific section of the configuration file.
     *
     * @return admin interface-specific configuration parameters
     * @since 2.0
     */
    @JsonProperty("admin")
    public AdminFactory getAdminFactory() {
        return admin;
    }

    /**
     * Sets the admin interface-specific section of the configuration file.
     *
     * @since 2.0
     */
    @JsonProperty("admin")
    public void setAdminFactory(AdminFactory admin) {
        this.admin = admin;
    }

    /**
     * Returns the health interface-specific section of the configuration file.
     *
     * @return health interface-specific configuration parameters
     * @since 2.1
     */
    @JsonProperty("health")
    public Optional<HealthFactory> getHealthFactory() {
        return Optional.ofNullable(health);
    }

    /**
     * Sets the health interface-specific section of the configuration file.
     *
     * @since 2.1
     */
    @JsonProperty("health")
    public void setHealthFactory(HealthFactory health) {
        this.health = health;
    }

    @Override
    public String toString() {
        return "Configuration{server=" + server + ", logging=" + logging + ", metrics=" + metrics + ", admin=" + admin + ", health=" + health + "}";
    }
}
