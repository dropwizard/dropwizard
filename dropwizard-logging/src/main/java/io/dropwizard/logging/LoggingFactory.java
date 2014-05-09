package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;

/**
 * Created by jrudd on 5/9/14.
 */
public interface LoggingFactory {
    void configure(MetricRegistry metricRegistry, String name);

    void stop();
}
