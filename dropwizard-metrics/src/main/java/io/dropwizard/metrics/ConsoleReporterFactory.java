package io.dropwizard.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotNull;
import java.io.PrintStream;
import java.util.TimeZone;

/**
 * A factory for configuring and building {@link ConsoleReporter} instances.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>timeZone</td>
 *         <td>UTC</td>
 *         <td>The timezone to display dates/times for.</td>
 *     </tr>
 *     <tr>
 *         <td>output</td>
 *         <td>stdout</td>
 *         <td>The stream to write to. One of {@code stdout} or {@code stderr}.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseFormattedReporterFactory} for more options.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseReporterFactory} for more options.</td>
 *     </tr>
 * </table>
 */
@JsonTypeName("console")
public class ConsoleReporterFactory extends BaseFormattedReporterFactory {
    @SuppressWarnings("UnusedDeclaration")
    public enum ConsoleStream {
        STDOUT(System.out),
        STDERR(System.err);

        private final PrintStream printStream;

        ConsoleStream(PrintStream printStream) {
            this.printStream = printStream;
        }

        public PrintStream get() {
            return printStream;
        }
    }

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @NotNull
    private ConsoleStream output = ConsoleStream.STDOUT;

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty
    public ConsoleStream getOutput() {
        return output;
    }

    @JsonProperty
    public void setOutput(ConsoleStream stream) {
        this.output = stream;
    }

    public ScheduledReporter build(MetricRegistry registry) {
        return ConsoleReporter.forRegistry(registry)
                              .convertDurationsTo(getDurationUnit())
                              .convertRatesTo(getRateUnit())
                              .filter(getFilter())
                              .formattedFor(getLocale())
                              .formattedFor(getTimeZone())
                              .outputTo(getOutput().get())
                              .build();
    }
}
