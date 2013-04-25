package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.logback.InstrumentedAppender;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.TimeZone;

// TODO: 11/7/11 <coda> -- document LoggingFactory
// TODO: 11/7/11 <coda> -- test LoggingFactory

public class LoggingFactory {
    // initially configure for WARN+ console logging
    public static void bootstrap() {
        final Logger root = getCleanRoot();

        final LogFormatter formatter = new LogFormatter(root.getLoggerContext(),
                                                        TimeZone.getDefault());
        formatter.start();

        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(Level.WARN.toString());
        filter.start();

        final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.addFilter(filter);
        appender.setContext(root.getLoggerContext());
        appender.setLayout(formatter);
        appender.start();

        root.addAppender(appender);
    }

    private final String name;
    private final Iterable<LoggingOutput> outputs;
    private final Map<String, Level> levels;
    private final Level defaultLevel;

    public LoggingFactory(String name,
                          Iterable<LoggingOutput> outputs,
                          Map<String, Level> levels,
                          Level defaultLevel) {
        this.name = name;
        this.outputs = outputs;
        this.levels = levels;
        this.defaultLevel = defaultLevel;
    }

    public void configure(MetricRegistry metricRegistry) {
        hijackJDKLogging();

        final Logger root = configureLevels();

        for (LoggingOutput output : outputs) {
            root.addAppender(output.build(root.getLoggerContext(), name, null));
        }

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName objectName = new ObjectName("com.codahale.dropwizard:type=Logging");
            if (!server.isRegistered(objectName)) {
                server.registerMBean(new JMXConfigurator(root.getLoggerContext(),
                                                         server,
                                                         objectName),
                                     objectName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        configureInstrumentation(root, metricRegistry);
    }

    private void configureInstrumentation(Logger root, MetricRegistry metricRegistry) {
        final InstrumentedAppender appender = new InstrumentedAppender(metricRegistry);
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

        root.setLevel(defaultLevel);

        for (Map.Entry<String, Level> entry : levels.entrySet()) {
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
