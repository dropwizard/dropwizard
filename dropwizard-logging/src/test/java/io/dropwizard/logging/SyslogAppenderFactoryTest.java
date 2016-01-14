package io.dropwizard.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.core.Layout;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.logging.filter.NullFilterFactory;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class SyslogAppenderFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

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
        final LoggerContext context = new LoggerContext();
        final Layout<ILoggingEvent> layout = new DropwizardLayout(context, "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
        layout.start();

        final AsyncAppender wrapper = (AsyncAppender) new SyslogAppenderFactory()
                .build(context, "MyApplication", layout, new NullFilterFactory<ILoggingEvent>(), new AsyncLoggingEventAppenderFactory());
        assertThat(((SyslogAppender) wrapper.getAppender("syslog-appender")).getSuffixPattern())
                .matches("^MyApplication\\[\\d+\\].+");
    }

    @Test
    public void stackTracePatternCanBeSet() throws Exception {
        final LoggerContext context = new LoggerContext();
        final Layout<ILoggingEvent> layout = new DropwizardLayout(context, "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
        layout.start();

        final SyslogAppenderFactory syslogAppenderFactory = new SyslogAppenderFactory();
        syslogAppenderFactory.setStackTracePrefix("--->");
        final AsyncAppender wrapper = (AsyncAppender) syslogAppenderFactory
                .build(context, "MyApplication", layout, new NullFilterFactory<ILoggingEvent>(), new AsyncLoggingEventAppenderFactory());
        assertThat(((SyslogAppender) wrapper.getAppender("syslog-appender"))
                .getStackTracePattern()).isEqualTo("--->");
    }

    @Test
    public void appenderContextIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final LoggerContext context = root.getLoggerContext();
        final Layout<ILoggingEvent> layout = new DropwizardLayout(context, "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
        layout.start();

        final SyslogAppenderFactory appenderFactory = new SyslogAppenderFactory();
        final Appender<ILoggingEvent> appender = appenderFactory.build(context, "test", layout,
            new NullFilterFactory<ILoggingEvent>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getContext()).isEqualTo(root.getLoggerContext());
    }

    @Test
    public void appenderNameIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final LoggerContext context = root.getLoggerContext();
        final Layout<ILoggingEvent> layout = new DropwizardLayout(context, "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
        layout.start();

        final SyslogAppenderFactory appenderFactory = new SyslogAppenderFactory();
        final Appender<ILoggingEvent> appender = appenderFactory.build(context, "test", layout,
            new NullFilterFactory<ILoggingEvent>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getName()).isEqualTo("async-syslog-appender");
    }
}
