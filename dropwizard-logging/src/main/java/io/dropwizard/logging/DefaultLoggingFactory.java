package io.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.util.StatusPrinter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.logback.InstrumentedAppender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.layout.DropwizardLayoutFactory;
import io.dropwizard.logging.layout.LayoutFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

@JsonTypeName("default")
public class DefaultLoggingFactory implements LoggingFactory {
    private static final ReentrantLock MBEAN_REGISTRATION_LOCK = new ReentrantLock();
    private static final ReentrantLock CHANGE_LOGGER_CONTEXT_LOCK = new ReentrantLock();

    @NotNull
    private Level level = Level.INFO;

    @NotNull
    private ImmutableMap<String, JsonNode> loggers = ImmutableMap.of();

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory<ILoggingEvent>> appenders = ImmutableList.<AppenderFactory<ILoggingEvent>>of(
            new ConsoleAppenderFactory<>()
    );

    @JsonIgnore
    private final LoggerContext loggerContext;

    @JsonIgnore
    private final PrintStream configurationErrorsStream;

    public DefaultLoggingFactory() {
        this(LoggingUtil.getLoggerContext(), System.err);
    }

    @VisibleForTesting
    DefaultLoggingFactory(LoggerContext loggerContext, PrintStream configurationErrorsStream) {
        this.loggerContext = requireNonNull(loggerContext);
        this.configurationErrorsStream = requireNonNull(configurationErrorsStream);
    }

    @VisibleForTesting
    LoggerContext getLoggerContext() {
        return loggerContext;
    }

    @VisibleForTesting
    PrintStream getConfigurationErrorsStream() {
        return configurationErrorsStream;
    }

    @JsonProperty
    public Level getLevel() {
        return level;
    }

    @JsonProperty
    public void setLevel(Level level) {
        this.level = level;
    }

    @JsonProperty
    public ImmutableMap<String, JsonNode> getLoggers() {
        return loggers;
    }

    @JsonProperty
    public void setLoggers(Map<String, JsonNode> loggers) {
        this.loggers = ImmutableMap.copyOf(loggers);
    }

    @JsonProperty
    public ImmutableList<AppenderFactory<ILoggingEvent>> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(List<AppenderFactory<ILoggingEvent>> appenders) {
        this.appenders = ImmutableList.copyOf(appenders);
    }

    public void configure(MetricRegistry metricRegistry, String name) {
        LoggingUtil.hijackJDKLogging();

        CHANGE_LOGGER_CONTEXT_LOCK.lock();
        final Logger root;
        try {
            root = configureLoggers(name);
        } finally {
            CHANGE_LOGGER_CONTEXT_LOCK.unlock();
        }

        final LevelFilterFactory<ILoggingEvent> levelFilterFactory = new ThresholdLevelFilterFactory();
        final AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory = new AsyncLoggingEventAppenderFactory();
        final LayoutFactory<ILoggingEvent> layoutFactory = new DropwizardLayoutFactory();

        for (AppenderFactory<ILoggingEvent> output : appenders) {
            root.addAppender(output.build(loggerContext, name, layoutFactory, levelFilterFactory, asyncAppenderFactory));
        }

        StatusPrinter.setPrintStream(configurationErrorsStream);
        try {
            StatusPrinter.printIfErrorsOccured(loggerContext);
        } finally {
            StatusPrinter.setPrintStream(System.out);
        }

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        MBEAN_REGISTRATION_LOCK.lock();
        try {
            final ObjectName objectName = new ObjectName("io.dropwizard:type=Logging");
            if (!server.isRegistered(objectName)) {
                server.registerMBean(new JMXConfigurator(loggerContext,
                                server,
                                objectName),
                        objectName);
            }
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException |
                NotCompliantMBeanException | MBeanRegistrationException e) {
            throw new RuntimeException(e);
        } finally {
            MBEAN_REGISTRATION_LOCK.unlock();
        }

        configureInstrumentation(root, metricRegistry);
    }

    public void stop() {
        // Should acquire the lock to avoid concurrent listener changes
        CHANGE_LOGGER_CONTEXT_LOCK.lock();
        try {
            loggerContext.stop();
        } finally {
            CHANGE_LOGGER_CONTEXT_LOCK.unlock();
        }
    }

    private void configureInstrumentation(Logger root, MetricRegistry metricRegistry) {
        final InstrumentedAppender appender = new InstrumentedAppender(metricRegistry);
        appender.setContext(loggerContext);
        appender.start();
        root.addAppender(appender);
    }

    private Logger configureLoggers(String name) {
        final Logger root = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        loggerContext.reset();

        final LevelChangePropagator propagator = new LevelChangePropagator();
        propagator.setContext(loggerContext);
        propagator.setResetJUL(true);

        loggerContext.addListener(propagator);

        root.setLevel(level);

        final LevelFilterFactory<ILoggingEvent> levelFilterFactory = new ThresholdLevelFilterFactory();
        final AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory = new AsyncLoggingEventAppenderFactory();
        final LayoutFactory<ILoggingEvent> layoutFactory = new DropwizardLayoutFactory();

        for (Map.Entry<String, JsonNode> entry : loggers.entrySet()) {
            final Logger logger = loggerContext.getLogger(entry.getKey());
            final JsonNode jsonNode = entry.getValue();
            if (jsonNode.isTextual()) {
                // Just a level as a string
                logger.setLevel(Level.valueOf(jsonNode.asText()));
            } else if (jsonNode.isObject()) {
                // A level and an appender
                final LoggerConfiguration configuration;
                try {
                    configuration = Jackson.newObjectMapper().treeToValue(jsonNode, LoggerConfiguration.class);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Wrong format of logger '" + entry.getKey() + "'", e);
                }
                logger.setLevel(configuration.getLevel());
                logger.setAdditive(configuration.isAdditive());

                for (AppenderFactory<ILoggingEvent> appender : configuration.getAppenders()) {
                    logger.addAppender(appender.build(loggerContext, name, layoutFactory, levelFilterFactory, asyncAppenderFactory));
                }
            } else {
                throw new IllegalArgumentException("Unsupported format of logger '" + entry.getKey() + "'");
            }
        }

        return root;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("level", level)
                .add("loggers", loggers)
                .add("appenders", appenders)
                .toString();
    }
}
