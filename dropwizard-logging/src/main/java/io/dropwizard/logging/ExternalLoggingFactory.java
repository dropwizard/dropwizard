package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * A logging factory which does configure a logging infrastructure.
 * Useful when the users doesn't want to use the Dropwizard logging configuration abilities.
 */
@JsonTypeName("external")
public class ExternalLoggingFactory implements LoggingFactory {

    @Override
    public void configure(MetricRegistry metricRegistry, String name) {
    }

    @Override
    public void stop() {
    }

    @Override
    public void reset() {
    }
}
