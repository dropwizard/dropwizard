package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import com.google.common.base.Optional;
import com.yammer.dropwizard.logging.AsyncAppender;
import com.yammer.dropwizard.logging.LogbackFactory;
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

        final ConsoleConfiguration console = config.getConsoleConfiguration();
        if (console.isEnabled()) {
            root.addAppender(AsyncAppender.wrap(LogbackFactory.buildConsoleAppender(console,
                                                                                    root.getLoggerContext(),
                                                                                    console.getLogFormat())));
        }

        final FileConfiguration file = config.getFileConfiguration();
        if (file.isEnabled()) {
            root.addAppender(AsyncAppender.wrap(LogbackFactory.buildFileAppender(file,
                                                                                 root.getLoggerContext(),
                                                                                 file.getLogFormat())));
        }

        final SyslogConfiguration syslog = config.getSyslogConfiguration();
        if (syslog.isEnabled()) {
            root.addAppender(AsyncAppender.wrap(LogbackFactory.buildSyslogAppender(syslog,
                                                                                   root.getLoggerContext(),
                                                                                   name,
                                                                                   syslog.getLogFormat())));
        }



        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName objectName = new ObjectName("com.yammer:type=Logging");
            if (!server.isRegistered(objectName)) {
                server.registerMBean(new JMXConfigurator(root.getLoggerContext(),
                                                         server,
                                                         objectName),
                                     objectName);
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
