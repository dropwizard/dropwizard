package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.util.Size;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class FileAppenderFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(FileAppenderFactory.class);
    }

    @Test
    public void includesCallerData() {
        FileAppenderFactory fileAppenderFactory = new FileAppenderFactory();
        fileAppenderFactory.setArchive(false);
        AsyncAppender asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", null);
        assertThat(asyncAppender.isIncludeCallerData()).isFalse();

        fileAppenderFactory.setIncludeCallerData(true);
        asyncAppender = (AsyncAppender) fileAppenderFactory.build(new LoggerContext(), "test", null);
        assertThat(asyncAppender.isIncludeCallerData()).isTrue();
    }

    @Test
    public void isRolling() throws Exception {
        // the method we want to test is protected, so we need to override it so we can see it
        FileAppenderFactory fileAppenderFactory = new FileAppenderFactory() {
            @Override
            public FileAppender<ILoggingEvent> buildAppender(LoggerContext context) {
                return super.buildAppender(context);
            }
        };

        fileAppenderFactory.setCurrentLogFilename("logfile.log");
        fileAppenderFactory.setArchive(true);
        fileAppenderFactory.setArchivedLogFilenamePattern("example-%d.log.gz");
        assertThat(fileAppenderFactory.buildAppender(new LoggerContext())).isInstanceOf(RollingFileAppender.class);
    }

    @Test
    public void hasMaxFileSize() throws Exception {
        FileAppenderFactory fileAppenderFactory = new FileAppenderFactory();
        fileAppenderFactory.setCurrentLogFilename("logfile.log");
        fileAppenderFactory.setArchive(true);
        fileAppenderFactory.setMaxFileSize(Size.kilobytes(1));
        fileAppenderFactory.setArchivedLogFilenamePattern("example-%d-%i.log.gz");
        RollingFileAppender<ILoggingEvent> appender = (RollingFileAppender<ILoggingEvent>) fileAppenderFactory.buildAppender(new LoggerContext());

        assertThat(appender.getTriggeringPolicy()).isInstanceOf(SizeAndTimeBasedFNATP.class);
        assertThat(((SizeAndTimeBasedFNATP) appender.getTriggeringPolicy()).getMaxFileSize()).isEqualTo("1024");
    }

    @Test
    public void appenderContextIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final FileAppenderFactory appenderFactory = new FileAppenderFactory();
        appenderFactory.setArchivedLogFilenamePattern("example-%d.log.gz");
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", null);

        assertThat(appender.getContext()).isEqualTo(root.getLoggerContext());
    }

    @Test
    public void appenderNameIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final FileAppenderFactory appenderFactory = new FileAppenderFactory();
        appenderFactory.setArchivedLogFilenamePattern("example-%d.log.gz");
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", null);

        assertThat(appender.getName()).isEqualTo("async-file-appender");
    }
}
