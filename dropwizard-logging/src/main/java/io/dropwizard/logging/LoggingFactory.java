package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultLoggingFactory.class)
public interface LoggingFactory extends Discoverable {
    void configure(MetricRegistry metricRegistry, String name);

    /** Should flush all log messages but not disable logging */
    void stop();

    /** Mainly useful in testing to reset the logging to a sane default before
     *  the next test configures logging to a desired level. */
    void reset();
}
