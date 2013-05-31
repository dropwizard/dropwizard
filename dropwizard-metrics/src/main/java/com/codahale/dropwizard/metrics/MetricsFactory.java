package com.codahale.dropwizard.metrics;

import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.metrics.reporters.ManagedScheduledReporter;
import com.codahale.dropwizard.metrics.reporters.ReporterFactory;
import com.codahale.dropwizard.util.Duration;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * TODO (31/05/13): Document
 */
public class MetricsFactory {

    @Valid
    @NotNull
    private Duration frequency = Duration.seconds(1);

    @Valid
    @NotNull
    private ImmutableList<ReporterFactory> reporters = ImmutableList.of();

    @JsonProperty
    public ImmutableList<ReporterFactory> getReporters() {
        return reporters;
    }

    @JsonProperty
    public void setReporters(ImmutableList<ReporterFactory> reporters) {
        this.reporters = reporters;
    }

    @JsonProperty
    public Duration getFrequency() {
        return frequency;
    }

    @JsonProperty
    public void setFrequency(Duration frequency) {
        this.frequency = frequency;
    }

    public void configure(LifecycleEnvironment environment, MetricRegistry registry, String name) {
        for (ReporterFactory reporter : reporters) {
            environment.manage(new ManagedScheduledReporter(
                    reporter.build(registry),
                    reporter.getFrequency().or(getFrequency())));
        }
    }
}
