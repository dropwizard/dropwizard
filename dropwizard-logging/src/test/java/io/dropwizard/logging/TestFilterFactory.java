package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.filter.FilterFactory;

@JsonTypeName("test-filter-factory")
public class TestFilterFactory implements FilterFactory<ILoggingEvent> {

    @Override
    public Filter<ILoggingEvent> build() {
        return new Filter<ILoggingEvent>() {
            @Override
            public FilterReply decide(ILoggingEvent event) {
                return FilterReply.NEUTRAL;
            }
        };
    }
}
