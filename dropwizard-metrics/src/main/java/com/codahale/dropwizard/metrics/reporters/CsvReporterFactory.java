package com.codahale.dropwizard.metrics.reporters;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * TODO (30/05/13): Document
 */
@JsonTypeName("csv")
public class CsvReporterFactory extends BaseFormattedReporterFactory {

    @NotNull
    private File file;

    @JsonProperty
    public File getFile() {
        return file;
    }

    @JsonProperty
    public void setFile(File file) {
        this.file = file;
    }

    @Override
    public ScheduledReporter build(MetricRegistry registry) {
        return CsvReporter
                .forRegistry(registry)
                .convertDurationsTo(getDurationUnit())
                .convertDurationsTo(getRateUnit())
                .filter(getFilter())
                .formatFor(getLocale())
                .withClock(getClock().get())
                .build(getFile());
    }
}
