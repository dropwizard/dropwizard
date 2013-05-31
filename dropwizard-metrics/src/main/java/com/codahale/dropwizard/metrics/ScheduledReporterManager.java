package com.codahale.dropwizard.metrics;

import com.codahale.dropwizard.lifecycle.Managed;
import com.codahale.dropwizard.util.Duration;
import com.codahale.metrics.ScheduledReporter;

/**
 * Manages a {@link ScheduledReporter} lifecycle.
 */
public class ScheduledReporterManager implements Managed {
    private final ScheduledReporter reporter;
    private final Duration period;

    /**
     * Manages the given {@code reporter} by reporting with the given {@code period}.
     *
     * @param reporter the reporter to manage.
     * @param period the frequency to report metrics at.
     */
    public ScheduledReporterManager(ScheduledReporter reporter, Duration period) {
        this.reporter = reporter;
        this.period = period;
    }

    /**
     * Begins reporting metrics using the configured {@link ScheduledReporter}.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        reporter.start(period.getQuantity(), period.getUnit());
    }

    /**
     * Stops the configured {@link ScheduledReporter} from reporting metrics.
     *
     * @throws Exception
     */
    @Override
    public void stop() throws Exception {
        reporter.stop();
    }
}
