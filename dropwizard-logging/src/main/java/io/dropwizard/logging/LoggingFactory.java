package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;

/**
 * A factory for configuring Logback logging.
 *
 * @see DefaultLoggingFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultLoggingFactory.class)
public interface LoggingFactory extends Discoverable {
    /**
     * Configure Logback logging
     * @param metricRegistry
     *          Where logging metrics should be registered if any.
     * @param applicationName
     *          The name of the application.
     */
    void configure(MetricRegistry metricRegistry, String applicationName);

    /**
     * Cleanup any resources created for logging.
     */
    void stop();
}
