package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Implementation of {@see LoggingFactory} which does nothing. This class should be used
 * when you want to let the normal Logback facilities be used for initializing a logging
 * environment.
 */
@JsonTypeName("noop")
public class NoOpLoggingFactory implements LoggingFactory {
    @Override
    public void configure(final MetricRegistry metricRegistry, final String applicationName) {
        // nothing to it
    }

    @Override
    public void stop() {
    }
}
