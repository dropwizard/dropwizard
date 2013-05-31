package com.codahale.dropwizard.metrics.reporters;

import com.codahale.metrics.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

import javax.validation.constraints.NotNull;
import java.io.PrintStream;
import java.util.TimeZone;

/**
 * A factory for configuring and building {@link ConsoleReporter}s.
 */
@JsonTypeName("console")
public class ConsoleReporterFactory extends BaseFormattedReporterFactory {

    public static enum ConsoleStream {
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
    private ConsoleStream consoleStream = ConsoleStream.STDOUT;


    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty
    public ConsoleStream getConsoleStream() {
        return consoleStream;
    }

    @JsonProperty
    public void setConsoleStream(ConsoleStream stream) {
        this.consoleStream = stream;
    }

    public ScheduledReporter build(MetricRegistry registry) {
        return ConsoleReporter
                .forRegistry(registry)
                .convertDurationsTo(getDurationUnit())
                .convertRatesTo(getRateUnit())
                .filter(getFilter())
                .formattedFor(getLocale())
                .formattedFor(getTimeZone())
                .outputTo(getConsoleStream().get())
                .withClock(getClock().get())
                .build();
    }
}
