package com.codahale.dropwizard.metrics;

import com.codahale.metrics.CsvReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotNull;
import java.io.File;

/**
 * A factory for configuring and building {@link CsvReporter} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>file</td>
 *         <td>No default. You must define a file.</td>
 *         <td>The CSV file to write metrics to.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseFormattedReporterFactory} for more options.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseReporterFactory} for more options.</td>
 *     </tr>
 * </table>
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
        return CsvReporter.forRegistry(registry)
                          .convertDurationsTo(getDurationUnit())
                          .convertDurationsTo(getRateUnit())
                          .filter(getFilter())
                          .formatFor(getLocale())
                          .build(getFile());
    }
}
