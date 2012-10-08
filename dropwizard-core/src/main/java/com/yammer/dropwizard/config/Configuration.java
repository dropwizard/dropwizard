package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;

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
@SuppressWarnings("FieldMayBeFinal")
public class Configuration {
    @Valid
    @NotNull
    @JsonProperty
    protected HttpConfiguration http = new HttpConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    protected LoggingConfiguration logging = new LoggingConfiguration();

    /**
     * Returns the HTTP-specific section of the configuration file.
     *
     * @return HTTP-specific configuration parameters
     */
    public HttpConfiguration getHttpConfiguration() {
        return http;
    }

    /**
     * Returns the logging-specific section of the configuration file.
     *
     * @return logging-specific configuration parameters
     */
    public LoggingConfiguration getLoggingConfiguration() {
        return logging;
    }
}
