package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
 *     public String getName() {
 *         return name;
 *     }
 *
 *     public int getAge() {
 *         return age;
 *     }
 * }
 * </pre>
 * Dropwizard will parse the given YAML file and provide an {@code ExampleConfiguration} instance
 * to your service whose {@code getName()} method will return {@code "Random Person"} and whose
 * {@code getAge()} method will return {@code 43}.
 *
 * @see <a href="http://www.yaml.org/YAML_for_ruby.html">YAML Cookbook</a>
 */
@SuppressWarnings("UnusedDeclaration")
public class Configuration {
    @Valid
    @NotNull
    @JsonProperty("http")
    private HttpConfiguration httpConfiguration = new HttpConfiguration();

    @Valid
    @NotNull
    @JsonProperty("logging")
    private LoggingConfiguration loggingConfiguration = new LoggingConfiguration();

    /**
     * Returns the HTTP-specific section of the configuration file.
     *
     * @return HTTP-specific configuration parameters
     */
    public HttpConfiguration getHttpConfiguration() {
        return httpConfiguration;
    }

    /**
     * Sets the HTTP-specific section of the configuration file.
     */
    public void setHttpConfiguration(HttpConfiguration config) {
        this.httpConfiguration = config;
    }

    /**
     * Returns the logging-specific section of the configuration file.
     *
     * @return logging-specific configuration parameters
     */
    public LoggingConfiguration getLoggingConfiguration() {
        return loggingConfiguration;
    }

    /**
     * Sets the logging-specific section of the configuration file.
     */
    public void setLoggingConfiguration(LoggingConfiguration config) {
        this.loggingConfiguration = config;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if ((obj == null) || (getClass() != obj.getClass())) { return false; }
        final Configuration that = (Configuration) obj;
        return httpConfiguration.equals(that.httpConfiguration) && loggingConfiguration.equals(that.loggingConfiguration);
    }

    @Override
    public int hashCode() {
        int result = httpConfiguration.hashCode();
        result = (31 * result) + loggingConfiguration.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("http", httpConfiguration).add("logging",
                                                                               loggingConfiguration).toString();
    }
}
