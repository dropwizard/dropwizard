package io.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.util.Duration;

import java.util.Optional;

/**
 * A service provider interface for creating metrics {@link ScheduledReporter reporters}.
 * <p/>
 * To create your own, just:
 * <ol>
 *     <li>Create a class which implements {@link ReporterFactory}.</li>
 *     <li>Annotate it with {@code @JsonTypeName} and give it a unique type name.</li>
 *     <li>Add a {@code META-INF/services/io.dropwizard.metrics.ReporterFactory}
 *     file with your implementation's full class name to the class path.</li>
 * </ol>
 *
 * @see ConsoleReporterFactory
 * @see CsvReporterFactory
 * @see Slf4jReporterFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface ReporterFactory extends Discoverable {
    /**
     * Returns the frequency for reporting metrics.
     *
     * @return the frequency for reporting metrics.
     */
    Optional<Duration> getFrequency();

    /**
     * Configures and builds a {@link ScheduledReporter} instance for the given registry.
     *
     * @param registry the metrics registry to report metrics from.
     *
     * @return a reporter configured for the given metrics registry.
     */
    ScheduledReporter build(MetricRegistry registry);
}
