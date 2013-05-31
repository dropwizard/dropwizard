package com.codahale.dropwizard.metrics.reporters;

import com.codahale.metrics.Clock;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Locale;

/**
 * A base {@link ReporterFactory} for configuring metric reporters with formatting options.
 * <p/>
 * Configures formatting options common to some {@link com.codahale.metrics.ScheduledReporter}s.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>locale</td>
 *         <td>System default {@link Locale}.</td>
 *         <td>The {@link Locale} for formatting numbers, dates and times.</td>
 *     </tr>
 *     <tr>
 *         <td>clock</td>
 *         <td>default</td>
 *         <td>The {@link Clock} to use for timing data. One of {@code user}, {@code cpu} or
 *         {@code default}.</td>
 *     </tr>
 *     <tr>
 *         <td colspan="3">See {@link BaseReporterFactory} for more options.</td>
 *     </tr>
 * </table>
 */
public abstract class BaseFormattedReporterFactory extends BaseReporterFactory {

    /**
     * An enumeration of configurable {@link Clock clocks}.
     */
    public static enum MetricClock {
        USER(new Clock.UserTimeClock()),
        CPU(new Clock.CpuTimeClock()),
        DEFAULT(Clock.defaultClock());

        private final Clock clock;

        MetricClock(Clock clock) {
            this.clock = clock;
        }

        public Clock get() {
            return clock;
        }
    }

    @NotNull
    private Locale locale = Locale.getDefault();

    @NotNull
    private MetricClock clock = MetricClock.DEFAULT;

    @JsonProperty
    public Locale getLocale() {
        return locale;
    }

    @JsonProperty
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @JsonProperty
    public MetricClock getClock() {
        return clock;
    }

    @JsonProperty
    public void setClock(MetricClock clock) {
        this.clock = clock;
    }
}
