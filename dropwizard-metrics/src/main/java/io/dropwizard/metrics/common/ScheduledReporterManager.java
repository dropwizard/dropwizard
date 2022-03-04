package io.dropwizard.metrics.common;

import com.codahale.metrics.ScheduledReporter;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;

/**
 * Manages a {@link ScheduledReporter} lifecycle.
 */
public class ScheduledReporterManager implements Managed {
    private final ScheduledReporter reporter;
    private final Duration period;
    private final boolean reportOnStop;

    /**
     * Manages the given {@code reporter} by reporting with the given {@code period}.
     *
     * @param reporter the reporter to manage.
     * @param period   the frequency to report metrics at.
     * @see #ScheduledReporterManager(ScheduledReporter, Duration, boolean)
     */
    public ScheduledReporterManager(ScheduledReporter reporter, Duration period) {
        this(reporter, period, false);
    }

    /**
     * Manages the given {@code reporter} by reporting with the given {@code period}.
     *
     * @param reporter     the reporter to manage.
     * @param period       the frequency to report metrics at.
     * @param reportOnStop whether the reporter should send one last report upon stopping
     * @since 2.0
     */
    public ScheduledReporterManager(ScheduledReporter reporter, Duration period, boolean reportOnStop) {
        this.reporter = reporter;
        this.period = period;
        this.reportOnStop = reportOnStop;
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
        try {
            if (reportOnStop) {
                reporter.report();
            }
        } finally {
            reporter.stop();
        }
    }
}
