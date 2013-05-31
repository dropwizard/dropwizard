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
 * A {@link ReporterFactory} for the {@link Slf4jReporter}.
 *
 * Configures and builds {@link Slf4jReporter}s for regularly reporting metrics via SLF4J.
 *
 * TODO: add configurable Marker support
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
