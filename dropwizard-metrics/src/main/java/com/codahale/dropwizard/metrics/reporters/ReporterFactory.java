package com.codahale.dropwizard.metrics.reporters;

import com.codahale.dropwizard.jackson.Discoverable;
import com.codahale.dropwizard.util.Duration;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.google.common.base.Optional;

/**
 * TODO (30/05/13): Document
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface ReporterFactory extends Discoverable {

    Optional<Duration> getFrequency();

    ScheduledReporter build(MetricRegistry registry);
}
