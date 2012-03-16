package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterAttachable;
import com.yammer.dropwizard.logging.LogFormatter;
import com.yammer.dropwizard.logging.LoggingBean;
import com.yammer.metrics.logback.InstrumentedAppender;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.TimeZone;

import static com.yammer.dropwizard.config.LoggingConfiguration.ConsoleConfiguration;
import static com.yammer.dropwizard.config.LoggingConfiguration.FileConfiguration;
import static com.yammer.dropwizard.config.LoggingConfiguration.SyslogConfiguration;

// TODO: 11/7/11 <coda> -- document LoggingFactory
// TODO: 11/7/11 <coda> -- test LoggingFactory

public class LoggingFactory {
    public static void bootstrap() {
        // initially configure for WARN+ console logging
        addConsoleAppender(getCleanRoot(), TimeZone.getDefault(), Level.WARN);
    }

    private final LoggingConfiguration config;

    public LoggingFactory(LoggingConfiguration config) {
        this.config = config;
    }

    public void configure() {
        hijackJDKLogging();

        final Logger root = configureLevels();

        configureConsoleLogging(root);
        configureFileLogging(root);
        configureSyslogLogging(root);

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName name = new ObjectName("com.yammer:type=Logging");
            server.registerMBean(new LoggingBean(), name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        configureInstrumentation(root);
    }

    private void configureInstrumentation(Logger root) {
        final InstrumentedAppender appender = new InstrumentedAppender();
        appender.setContext(root.getLoggerContext());
        appender.start();
        root.addAppender(appender);
    }

    private void hijackJDKLogging() {
        final java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        for (java.util.logging.Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }
        root.addHandler(new SLF4JBridgeHandler());
    }

    private Logger configureLevels() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.getLoggerContext().reset();
        root.setLevel(config.getLevel());

        for (Map.Entry<String, Level> entry : config.getLoggers().entrySet()) {
            ((Logger) LoggerFactory.getLogger(entry.getKey())).setLevel(entry.getValue());
        }

        return root;
    }

    private void configureSyslogLogging(Logger root) {
        final SyslogConfiguration syslog = config.getSyslogConfiguration();
        if (syslog.isEnabled()) {
            final PatternLayout layout = new PatternLayout();
            layout.setPattern("%c: %m");
            layout.start();

            final SyslogAppender a = new SyslogAppender();
            a.setLayout(layout);
            a.setSyslogHost(syslog.getHost());
            a.setFacility(syslog.getFacility());
            addThresholdFilter(a, syslog.getThreshold());
            root.addAppender(a);
        }
    }

    private void configureFileLogging(Logger root) {
        final FileConfiguration file = config.getFileConfiguration();
        if (file.isEnabled()) {
            final DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> triggeringPolicy =
                    new DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent>();
            triggeringPolicy.setContext(root.getLoggerContext());

            final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
            rollingPolicy.setContext(root.getLoggerContext());
            rollingPolicy.setFileNamePattern(file.getArchivedLogFilenamePattern());
            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(
                    triggeringPolicy);
            triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
            rollingPolicy.setMaxHistory(file.getArchivedFileCount());

            final LogFormatter formatter = new LogFormatter(root.getLoggerContext(),
                                                            file.getTimeZone());
            formatter.start();

            final RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<ILoggingEvent>();
            appender.setAppend(true);
            appender.setContext(root.getLoggerContext());
            appender.setLayout(formatter);
            appender.setFile(file.getCurrentLogFilename());
            appender.setPrudent(false);
            appender.setRollingPolicy(rollingPolicy);
            appender.setTriggeringPolicy(triggeringPolicy);
            addThresholdFilter(appender, file.getThreshold());

            rollingPolicy.setParent(appender);
            rollingPolicy.start();

            appender.stop();
            appender.start();

            root.addAppender(appender);
        }
    }

    private void configureConsoleLogging(Logger root) {
        final ConsoleConfiguration console = config.getConsoleConfiguration();
        if (console.isEnabled()) {
            addConsoleAppender(root, console.getTimeZone(), console.getThreshold());
        }
    }

    private static void addConsoleAppender(Logger root, TimeZone timeZone, Level threshold) {
        final LogFormatter formatter = new LogFormatter(root.getLoggerContext(),
                                                        timeZone);
        formatter.start();

        final ConsoleAppender<ILoggingEvent> a = new ConsoleAppender<ILoggingEvent>();
        a.setContext(root.getLoggerContext());
        a.setLayout(formatter);
        addThresholdFilter(a, threshold);
        a.start();

        root.addAppender(a);
    }

    private static void addThresholdFilter(FilterAttachable<ILoggingEvent> appender, Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }

    private static Logger getCleanRoot() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();
        return root;
    }
}
