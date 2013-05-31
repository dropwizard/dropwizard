package com.codahale.dropwizard.metrics.reporters;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Slf4jReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *         <td colspan="3">See {@link BaseFormattedReporterFactory} for more options.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseReporterFactory} for more options.</td>
 *     </tr>
 * </table>
 * 
 * @todo add configurable Marker support
 */
@JsonTypeName("log")
public class Slf4jReporterFactory extends BaseReporterFactory {

    @NotEmpty
    private String loggerName = "metrics";

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

    public ScheduledReporter build(MetricRegistry registry) {
        return Slf4jReporter
                .forRegistry(registry)
                .convertDurationsTo(getDurationUnit())
                .convertRatesTo(getRateUnit())
                .filter(getFilter())
                .outputTo(getLogger())
                .build();
    }
}
