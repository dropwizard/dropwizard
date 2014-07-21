package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class SyslogAppenderFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(SyslogAppenderFactory.class);
    }

    @Test
    public void defaultIncludesAppName() throws Exception {
        assertThat(new SyslogAppenderFactory().getLogFormat())
                .contains("%app");
    }

    @Test
    public void defaultIncludesPid() throws Exception {
        assertThat(new SyslogAppenderFactory().getLogFormat())
                .contains("%pid");
    }

    @Test
    public void patternIncludesAppNameAndPid() throws Exception {
        Appender<ILoggingEvent> wrapper = new SyslogAppenderFactory()
                .build(new LoggerContext(), "MyApplication", null);

        // hack to get at the SyslogAppender beneath the AsyncAppender
        // todo: find a nicer way to do all this
        Field delegate = AsyncAppenderBase.class.getDeclaredField("aai");
        delegate.setAccessible(true);
        SyslogAppender appender = (SyslogAppender) ((AppenderAttachableImpl) delegate.get(wrapper)).iteratorForAppenders().next();

        assertThat(appender.getSuffixPattern())
                .matches("^MyApplication\\[\\d+\\].+");
    }

    @Test
    public void stackTracePatternCanBeSet() throws Exception {
        SyslogAppenderFactory syslogAppenderFactory = new SyslogAppenderFactory();
        syslogAppenderFactory.setStackTracePrefix("--->");
        Appender<ILoggingEvent> wrapper = syslogAppenderFactory.build(new LoggerContext(), "MyApplication", null);

        // hack to get at the SyslogAppender beneath the AsyncAppender
        // todo: find a nicer way to do all this
        Field delegate = AsyncAppenderBase.class.getDeclaredField("aai");
        delegate.setAccessible(true);
        SyslogAppender appender = (SyslogAppender) ((AppenderAttachableImpl) delegate.get(wrapper)).iteratorForAppenders().next();

        assertThat(appender.getStackTracePattern())
                .isEqualTo("--->");
    }
}
