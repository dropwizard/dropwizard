package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.LayoutBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.layout.DiscoverableLayoutFactory;

import java.util.TimeZone;

@JsonTypeName("test")
public class TestLayoutFactory implements DiscoverableLayoutFactory<ILoggingEvent> {

    private boolean includeSeparator;

    @Override
    public LayoutBase<ILoggingEvent> build(LoggerContext context, TimeZone timeZone) {
        return new TestLayout();
    }

    @JsonProperty
    public boolean isIncludeSeparator() {
        return includeSeparator;
    }

    @JsonProperty
    public void setIncludeSeparator(boolean includeSeparator) {
        this.includeSeparator = includeSeparator;
    }

    public static class TestLayout extends LayoutBase<ILoggingEvent> {
        @Override
        public String doLayout(ILoggingEvent event) {
            return "TEST!\n";
        }
    }
}
