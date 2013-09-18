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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.management.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class LoggingFactory {
    // initially configure for WARN+ console logging
    public static void bootstrap() {
        bootstrap(Level.WARN);
    }

    public static void bootstrap(Level level) {
        hijackJDKLogging();

        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();

        final DropwizardLayout formatter = new DropwizardLayout(root.getLoggerContext(),
                                                        TimeZone.getDefault());
        formatter.start();

        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(level.toString());
        filter.start();

        final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.addFilter(filter);
        appender.setContext(root.getLoggerContext());
        appender.setLayout(formatter);
        appender.start();

        root.addAppender(appender);
    }

    private static void hijackJDKLogging() {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @NotNull
    private Level level = Level.INFO;

    @NotNull
    private ImmutableMap<String, Level> loggers = ImmutableMap.of();

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory> appenders = ImmutableList.<AppenderFactory>of(
            new ConsoleAppenderFactory()
    );

    @JsonProperty
    public Level getLevel() {
        return level;
    }

    @JsonProperty
    public void setLevel(Level level) {
        this.level = level;
    }

    @JsonProperty
    public ImmutableMap<String, Level> getLoggers() {
        return loggers;
    }

    @JsonProperty
    public void setLoggers(Map<String, Level> loggers) {
        this.loggers = ImmutableMap.copyOf(loggers);
    }

    @JsonProperty
    public ImmutableList<AppenderFactory> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(List<AppenderFactory> appenders) {
        this.appenders = ImmutableList.copyOf(appenders);
    }

    public void configure(MetricRegistry metricRegistry, String name) {
        hijackJDKLogging();

        final Logger root = configureLevels();

        for (AppenderFactory output : appenders) {
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
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException |
                NotCompliantMBeanException | MBeanRegistrationException e) {
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

    private Logger configureLevels() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.getLoggerContext().reset();

        final LevelChangePropagator propagator = new LevelChangePropagator();
        propagator.setContext(root.getLoggerContext());
        propagator.setResetJUL(true);

        root.getLoggerContext().addListener(propagator);

        root.setLevel(level);

        for (Map.Entry<String, Level> entry : loggers.entrySet()) {
            ((Logger) LoggerFactory.getLogger(entry.getKey())).setLevel(entry.getValue());
        }

        return root;
    }
}
