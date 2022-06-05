package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Discoverable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultLoggingFactory.class)
public interface LoggingFactory extends Discoverable {
    default void configure(MetricRegistry metricRegistry, String name) {
    }

    default void configure(ObjectMapper objectMapper, MetricRegistry metricRegistry, String name) {
        configure(metricRegistry, name);
    }

    /** Should flush all log messages but not disable logging */
    void stop();

    /** Mainly useful in testing to reset the logging to a sane default before
     *  the next test configures logging to a desired level. */
    void reset();
}
