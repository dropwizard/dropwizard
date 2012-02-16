package com.yammer.dropwizard.config;

import com.yammer.dropwizard.logging.LogFormatter;
import com.yammer.dropwizard.logging.LoggingBean;
import com.yammer.metrics.log4j.InstrumentedAppender;
import org.apache.log4j.*;
import org.apache.log4j.net.SyslogAppender;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;

import static com.yammer.dropwizard.config.LoggingConfiguration.*;

// TODO: 11/7/11 <coda> -- document LoggingFactory
// TODO: 11/7/11 <coda> -- test LoggingFactory

public class LoggingFactory {
    public static void bootstrap() {
        final ConsoleAppender appender = new ConsoleAppender(new LogFormatter(UTC));
        appender.setThreshold(Level.WARN);
        Logger.getRootLogger().addAppender(appender);
    }

    private final LoggingConfiguration config;

    public LoggingFactory(LoggingConfiguration config) {
        this.config = config;
    }

    public void configure() {
        hijackJDKLogging();

        final Logger root = configureLevels();

        final AsyncAppender appender = new AsyncAppender();
        root.addAppender(appender);

        configureConsoleLogging(appender);
        configureFileLogging(appender);
        configureSyslogLogging(appender);

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName name = new ObjectName("com.yammer:type=Logging");
            server.registerMBean(new LoggingBean(), name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // add in an instrumented null appender to get full logging stats
        LogManager.getRootLogger().addAppender(new InstrumentedAppender());
    }

    private void hijackJDKLogging() {
        final java.util.logging.Logger root = java.util.logging.Logger.getLogger("");
        for (java.util.logging.Handler handler : root.getHandlers()) {
            root.removeHandler(handler);
        }
        root.addHandler(new SLF4JBridgeHandler());
    }

    private Logger configureLevels() {
        final Logger root = Logger.getRootLogger();
        root.getLoggerRepository().resetConfiguration();
        root.setLevel(config.getLevel());

        for (Map.Entry<String, Level> entry : config.getLoggers().entrySet()) {
            Logger.getLogger(entry.getKey()).setLevel(entry.getValue());
        }

        return root;
    }

    private void configureSyslogLogging(AsyncAppender appender) {
        final SyslogConfiguration syslog = config.getSyslogConfiguration();
        if (syslog.isEnabled()) {
            final Layout layout = new PatternLayout("%c: %m");
            final SyslogAppender a = new SyslogAppender();
            a.setLayout(layout);
            a.setSyslogHost(syslog.getHost());
            a.setFacility(syslog.getFacility());
            a.setThreshold(syslog.getThreshold());
            appender.addAppender(a);
        }
    }

    private void configureFileLogging(AsyncAppender appender) {
        final FileConfiguration file = config.getFileConfiguration();
        if (file.isEnabled()) {
            final RollingFileAppender a = new RollingFileAppender();
            a.setLayout(new LogFormatter(file.getTimeZone()));
            a.setAppend(true);
            a.setFile(file.getFilenamePattern());
            a.setMaximumFileSize(file.getMaxFileSize().toBytes());
            a.setMaxBackupIndex(file.getRetainedFileCount());
            a.setThreshold(file.getThreshold());
            a.activateOptions();
            appender.addAppender(a);
        }
    }

    private void configureConsoleLogging(AsyncAppender appender) {
        final ConsoleConfiguration console = config.getConsoleConfiguration();
        if (console.isEnabled()) {
            final ConsoleAppender a = new ConsoleAppender(new LogFormatter(console.getTimeZone()));
            a.setThreshold(console.getThreshold());
            appender.addAppender(a);
        }
    }
}
