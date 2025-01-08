package io.dropwizard.logging.common;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.pattern.DynamicConverter;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.common.layout.DiscoverableLayoutFactory;

import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;
import java.util.function.Supplier;

@JsonTypeName("test-pattern")
public class TestPatternLayoutFactory implements DiscoverableLayoutFactory<ILoggingEvent> {

    @Override
    public LayoutBase<ILoggingEvent> build(LoggerContext context, TimeZone timeZone) {
        return new TestPatternLayout(context);
    }

    public static class TestPatternLayout extends PatternLayoutBase<ILoggingEvent> {
        private TestPatternLayout(Context context) {
            super();
            setContext(context);
        }

        @Override
        public String doLayout(ILoggingEvent event) {
            return "TEST PATTERN!\n";
        }

        @Override
        protected Map<String,Supplier<DynamicConverter>> getDefaultConverterSupplierMap() {
            return Collections.emptyMap();
        }

        @Override
        public Map<String, String> getDefaultConverterMap() {
            return Collections.emptyMap();
        }
    }
}
