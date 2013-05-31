package com.codahale.dropwizard.metrics.reporters;

import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.util.Duration;
import com.codahale.metrics.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

/**
 * TODO (30/05/13): Document
 */
abstract public class BaseReporterFactory implements ReporterFactory {

    @NotNull
    private TimeUnit durationUnit = TimeUnit.MILLISECONDS;

    @NotNull
    private TimeUnit rateUnit = TimeUnit.SECONDS;

    @NotNull
    private ImmutableSet<String> excludes = ImmutableSet.of();

    @NotNull
    private ImmutableSet<String> includes = ImmutableSet.of();

    @NotNull
    @Valid
    private Optional<Duration> frequency = Optional.of(Duration.seconds(1));

    public TimeUnit getDurationUnit() {
        return durationUnit;
    }

    @JsonProperty
    public void setDurationUnit(TimeUnit durationUnit) {
        this.durationUnit = durationUnit;
    }

    @JsonProperty
    public TimeUnit getRateUnit() {
        return rateUnit;
    }

    @JsonProperty
    public void setRateUnit(final TimeUnit rateUnit) {
        this.rateUnit = rateUnit;
    }

    @JsonProperty
    public ImmutableSet<String> getIncludes() {
        return includes;
    }

    @JsonProperty
    public void setIncludes(ImmutableSet<String> includes) {
        this.includes = includes;
    }

    @JsonProperty
    public ImmutableSet<String> getExcludes() {
        return excludes;
    }

    @JsonProperty
    public void setExcludes(ImmutableSet<String> excludes) {
        this.excludes = excludes;
    }

    @JsonProperty
    public Optional<Duration> getFrequency() {
        return frequency;
    }

    @JsonProperty
    public void setFrequency(Optional<Duration> frequency) {
        this.frequency = frequency;
    }

    /**
     * Gets a {@link MetricFilter} that specifically includes and excludes configured metrics.
     * <p/>
     * <i>Included</i> metrics take precedence over <i>excluded</i> metrics; allowing you to include
     * previously excluded metrics.
     *
     * @return the filter for selecting metrics based on the configured excludes/includes.
     * @see #getIncludes()
     * @see #getExcludes()
     */
    public MetricFilter getFilter() {
        return new MetricFilter() {
            @Override
            public boolean matches(final String name, final Metric metric) {
                return getIncludes().isEmpty()
                        ? !getExcludes().contains(name)
                        : getIncludes().contains(name);
            }
        };
    }
}
