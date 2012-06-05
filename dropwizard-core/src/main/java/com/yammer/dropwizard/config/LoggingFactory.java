package com.yammer.dropwizard.config;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import com.google.common.base.Optional;
import com.yammer.dropwizard.logging.LogbackFactory;
import com.yammer.dropwizard.logging.LoggingBean;
import com.yammer.metrics.logback.InstrumentedAppender;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.TimeZone;

import static com.yammer.dropwizard.config.LoggingConfiguration.*;

// TODO: 11/7/11 <coda> -- document LoggingFactory
// TODO: 11/7/11 <coda> -- test LoggingFactory

public class LoggingFactory {
    public static void bootstrap() {
        // initially configure for WARN+ console logging
        final ConsoleConfiguration console = new ConsoleConfiguration();
        console.setEnabled(true);
        console.setTimeZone(TimeZone.getDefault());
        console.setThreshold(Level.WARN);

        final Logger root = getCleanRoot();
        root.addAppender(LogbackFactory.buildConsoleAppender(console,
                                                             root.getLoggerContext(),
                                                             Optional.<String>absent()));
    }

    private final LoggingConfiguration config;
    private final String name;

    public LoggingFactory(LoggingConfiguration config, String name) {
        this.config = config;
        this.name = name;
    }

    public void configure() {
        hijackJDKLogging();

        final Logger root = configureLevels();

        final AppenderConfiguration appenderConfig = config.getAppenderConfiguration();
        final ConsoleConfiguration console = config.getConsoleConfiguration();
        if (console.isEnabled()) {
            AsyncAppender appender = (AsyncAppender) LogbackFactory.wrapAsync(LogbackFactory.buildConsoleAppender(console,
                                                                        root.getLoggerContext(),
                                                                        console.getLogFormat()));
            appender.setIncludeCallerData(appenderConfig.isIncludeCallerData());
            appender.setQueueSize(appenderConfig.getQueueSize());
            appender.setDiscardingThreshold(appenderConfig.getDiscardingThreshold());
            root.addAppender(appender);
        }

        final FileConfiguration file = config.getFileConfiguration();
        if (file.isEnabled()) {
            AsyncAppender appender = (AsyncAppender) LogbackFactory.wrapAsync(LogbackFactory.buildFileAppender(file,
                                                                                    root.getLoggerContext(),
                                                                                    file.getLogFormat()));
            appender.setIncludeCallerData(appenderConfig.isIncludeCallerData());
            appender.setQueueSize(appenderConfig.getQueueSize());
            appender.setDiscardingThreshold(appenderConfig.getDiscardingThreshold());
            root.addAppender(appender);
        }

        final SyslogConfiguration syslog = config.getSyslogConfiguration();
        if (syslog.isEnabled()) {
            AsyncAppender appender = (AsyncAppender) LogbackFactory.wrapAsync(LogbackFactory.buildSyslogAppender(syslog,
                                                                                                root.getLoggerContext(),
                                                                                                name,
                                                                                                syslog.getLogFormat()));
            appender.setIncludeCallerData(appenderConfig.isIncludeCallerData());
            appender.setQueueSize(appenderConfig.getQueueSize());
            appender.setDiscardingThreshold(appenderConfig.getDiscardingThreshold());
            root.addAppender(appender);
        }

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName objectName = new ObjectName("com.yammer:type=Logging");
            if (!server.isRegistered(objectName)) {
                server.registerMBean(new LoggingBean(), objectName);
            }
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
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private Logger configureLevels() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.getLoggerContext().reset();

        final LevelChangePropagator propagator = new LevelChangePropagator();
        propagator.setContext(root.getLoggerContext());
        propagator.setResetJUL(true);

        root.getLoggerContext().addListener(propagator);

        root.setLevel(config.getLevel());

        for (Map.Entry<String, Level> entry : config.getLoggers().entrySet()) {
            ((Logger) LoggerFactory.getLogger(entry.getKey())).setLevel(entry.getValue());
        }

        return root;
    }

    private static Logger getCleanRoot() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();
        return root;
    }
}
