package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A no-op logging factory to use when logging is configured independently of Dropwizard.
 */
@JsonTypeName("external")
public class ExternalLoggingFactory implements LoggingFactory {

    @Override
    public void configure(MetricRegistry metricRegistry, String name) {
        // Do nothing
    }

    @Override
    public void stop() {
        // Do nothing
    }

    @Override
    public void reset() {
        // Do nothing
    }
}
