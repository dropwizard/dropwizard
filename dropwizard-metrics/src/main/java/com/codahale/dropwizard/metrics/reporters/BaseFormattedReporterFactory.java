package com.codahale.dropwizard.metrics.reporters;

import com.codahale.metrics.Clock;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Locale;

/**
 * TODO (31/05/13): Document
 */
public abstract class BaseFormattedReporterFactory extends BaseReporterFactory {

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
