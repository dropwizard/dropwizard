package io.dropwizard.metrics.common;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.util.Duration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
 *         <td>1 minute</td>
 *         <td>The frequency to report metrics. Overridable per-reporter.</td>
 *     </tr>
 *     <tr>
 *         <td>reporters</td>
 *         <td>No reporters.</td>
 *         <td>A list of {@link ReporterFactory reporters} to report metrics.</td>
 *     </tr>
 *     <tr>
 *         <td>reportOnStop</td>
 *         <td>{@code false}</td>
 *         <td>To report metrics one last time when stopping Dropwizard.</td>
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
    private List<ReporterFactory> reporters = Collections.emptyList();

    private boolean reportOnStop = false;

    @JsonProperty
    public List<ReporterFactory> getReporters() {
        return reporters;
    }

    @JsonProperty
    public void setReporters(List<ReporterFactory> reporters) {
        this.reporters = new ArrayList<>(reporters);
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
     * @since 2.0
     */
    @JsonProperty
    public boolean isReportOnStop() {
        return reportOnStop;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setReportOnStop(boolean reportOnStop) {
        this.reportOnStop = reportOnStop;
    }

    /**
     * Configures the given lifecycle with the {@link com.codahale.metrics.ScheduledReporter
     * reporters} configured for the given registry.
     * <p />
     * The reporters are tied in to the given lifecycle, such that their {@link #getFrequency()
     * frequency} for reporting metrics begins when the lifecycle {@link
     * io.dropwizard.lifecycle.Managed#start() starts}, and stops when the lifecycle
     * {@link io.dropwizard.lifecycle.Managed#stop() stops}.
     *
     * @param environment the lifecycle to manage the reporters.
     * @param registry the metric registry to report metrics from.
     */
    public void configure(LifecycleEnvironment environment, MetricRegistry registry) {
        for (ReporterFactory reporter : reporters) {
            try {
                final ScheduledReporterManager manager =
                        new ScheduledReporterManager(reporter.build(registry),
                                                     reporter.getFrequency().orElseGet(this::getFrequency),
                                                     isReportOnStop());
                environment.manage(manager);
            } catch (Exception e) {
                LOGGER.warn("Failed to create reporter, metrics may not be properly reported.", e);
            }
        }
    }

    @Override
    public String toString() {
        return "MetricsFactory{frequency=" + frequency + ", reporters=" + reporters + ", reportOnStop=" + reportOnStop + '}';
    }
}
