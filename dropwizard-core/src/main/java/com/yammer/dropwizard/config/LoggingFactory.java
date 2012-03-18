package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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
        root.addAppender(LogbackFactory.buildConsoleAppender(console, root.getLoggerContext()));
    }

    private final LoggingConfiguration config;

    public LoggingFactory(LoggingConfiguration config) {
        this.config = config;
    }

    public void configure() {
        hijackJDKLogging();

        final Logger root = configureLevels();

        final ConsoleConfiguration console = config.getConsoleConfiguration();
        if (console.isEnabled()) {
            root.addAppender(LogbackFactory.buildConsoleAppender(console, root.getLoggerContext()));
        }

        final FileConfiguration file = config.getFileConfiguration();
        if (file.isEnabled()) {
            root.addAppender(LogbackFactory.buildFileAppender(file, root.getLoggerContext()));
        }

        final SyslogConfiguration syslog = config.getSyslogConfiguration();
        if (syslog.isEnabled()) {
            root.addAppender(LogbackFactory.buildSyslogAppender(syslog, root.getLoggerContext()));
        }

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

    private static Logger getCleanRoot() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();
        return root;
    }
}
