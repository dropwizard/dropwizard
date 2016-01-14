package io.dropwizard.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;

public class ThresholdFilterFactory implements FilterFactory<ILoggingEvent> {
    @Override
    public Filter<ILoggingEvent> build(Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        return filter;
    }
}
