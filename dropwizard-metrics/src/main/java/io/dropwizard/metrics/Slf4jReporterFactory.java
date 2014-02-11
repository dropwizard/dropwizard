package io.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MarkerFactory;

/**
 * A {@link ReporterFactory} for {@link Slf4jReporter} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>logger</td>
 *         <td>metrics</td>
 *         <td>The name of the logger to write metrics to.</td>
 *     </tr>
 *     <tr>
 *         <td>markerName</td>
 *         <td>(none)</td>
 *         <td>The name of the marker to mark logged metrics with.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseReporterFactory} for more options.</td>
 *     </tr>
 * </table>
 */
@JsonTypeName("log")
public class Slf4jReporterFactory extends BaseReporterFactory {
    @NotEmpty
    private String loggerName = "metrics";

    private String markerName;

    @JsonProperty("logger")
    public String getLoggerName() {
        return loggerName;
    }

    @JsonProperty("logger")
    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public Logger getLogger() {
        return LoggerFactory.getLogger(getLoggerName());
    }

    @JsonProperty
    public String getMarkerName() {
        return markerName;
    }

    @JsonProperty
    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }

    public ScheduledReporter build(MetricRegistry registry) {
        final Slf4jReporter.Builder builder = Slf4jReporter.forRegistry(registry)
                                                           .convertDurationsTo(getDurationUnit())
                                                           .convertRatesTo(getRateUnit())
                                                           .filter(getFilter())
                                                           .outputTo(getLogger());
        if (markerName != null) {
            builder.markWith(MarkerFactory.getMarker(markerName));
        }

        return builder.build();
    }
}
