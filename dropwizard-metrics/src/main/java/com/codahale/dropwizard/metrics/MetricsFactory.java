package com.codahale.dropwizard.metrics;

import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.metrics.ScheduledReporterManager;
import com.codahale.dropwizard.metrics.ReporterFactory;
import com.codahale.dropwizard.util.Duration;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * A factory for configuring the metrics sub-system for the environment.
 * <p/>
 * Configures an optional list of {@link com.codahale.metrics.ScheduledReporter reporters} with a
 * default {@link #frequency}.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>frequency</td>
 *         <td>1 second</td>
 *         <td>The frequency to report metrics. Overridable per-reporter.</td>
 *     </tr>
 *     <tr>
 *         <td>reporters</td>
 *         <td>No reporters.</td>
 *         <td>A list of {@link ReporterFactory reporters} to report metrics.</td>
 *     </tr>
 * </table>
 */
public class MetricsFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricsFactory.class);

    @Valid
    @NotNull
    private Duration frequency = Duration.minutes(1);

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

    /**
     * Configures the given lifecycle with the {@link com.codahale.metrics.ScheduledReporter
     * reporters} configured for the given registry.
     * <p />
     * The reporters are tied in to the given lifecycle, such that their {@link #getFrequency()
     * frequency} for reporting metrics begins when the lifecycle {@link
     * com.codahale.dropwizard.lifecycle.Managed#start() starts}, and stops when the lifecycle
     * {@link com.codahale.dropwizard.lifecycle.Managed#stop() stops}.
     *
     * @param environment the lifecycle to manage the reporters.
     * @param registry the metric registry to report metrics from.
     */
    public void configure(LifecycleEnvironment environment, MetricRegistry registry) {
        for (ReporterFactory reporter : reporters) {
            try {
                final ScheduledReporterManager manager =
                        new ScheduledReporterManager(reporter.build(registry),
                                                     reporter.getFrequency().or(getFrequency()));
                environment.manage(manager);
            } catch (Exception e) {
                LOGGER.warn("Failed to create reporter, metrics may not be properly reported.", e);
            }
        }
    }
}
