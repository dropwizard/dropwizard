package io.dropwizard.metrics;

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
 *         <td>No default. You must define a directory.</td>
 *         <td>The directory where the csv metrics will be written. If the
 *         directory does not exist on startup, an attempt will be made to
 *         create it. If the creation fails, then subsequent errors will logged
 *         whenever the metrics are performed.</td>
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
        // Attempt to create the directory for the metrics. If an exception
        // occurs, swallow it, and let metrics log the errors
        try {
            file.mkdir();
        }
        catch (Exception e) {
        }

        return CsvReporter.forRegistry(registry)
                          .convertDurationsTo(getDurationUnit())
                          .convertRatesTo(getRateUnit())
                          .filter(getFilter())
                          .formatFor(getLocale())
                          .build(getFile());
    }
}
