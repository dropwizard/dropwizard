package io.dropwizard.health;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultHealthFactory.class)
public interface HealthFactory extends Discoverable {
    void configure(final MetricRegistry metrics, final LifecycleEnvironment lifecycle,
                   final HealthCheckRegistry healthChecks, final ServletEnvironment servlets);
}
