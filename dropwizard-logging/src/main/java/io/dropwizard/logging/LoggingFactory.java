package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultLoggingFactory.class)
public interface LoggingFactory extends Discoverable {
    void configure(MetricRegistry metricRegistry, String name);

    void stop();
}
