package io.dropwizard.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;

/**
 * Factory that creates a {@link Filter} of type {@link ILoggingEvent}
 */
public class ThresholdLevelFilterFactory implements LevelFilterFactory<ILoggingEvent> {

    /**
     * Creates and starts a {@link Filter} for the given threshold.
     * @param threshold The minimum event level for this filter.
     * @return a new, started {@link Filter}
     */
    @Override
    public Filter<ILoggingEvent> build(Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        return filter;
    }
}
