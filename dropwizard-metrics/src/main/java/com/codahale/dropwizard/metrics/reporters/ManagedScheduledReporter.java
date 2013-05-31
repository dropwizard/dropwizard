package com.codahale.dropwizard.metrics.reporters;

import com.codahale.dropwizard.lifecycle.Managed;
import com.codahale.dropwizard.util.Duration;
import com.codahale.metrics.ScheduledReporter;

/**
 * Manages a {@link ScheduledReporter} lifecycle.
 */
public class ManagedScheduledReporter implements Managed {

    private final ScheduledReporter reporter;
    private final Duration period;

    public ManagedScheduledReporter(ScheduledReporter reporter, Duration period) {
        this.reporter = reporter;
        this.period = period;
    }

    @Override
    public void start() throws Exception {
        reporter.start(period.getQuantity(), period.getUnit());
    }

    @Override
    public void stop() throws Exception {
        reporter.stop();
    }
}
