package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
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
import com.google.common.collect.Lists;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.layout.DropwizardLayoutFactory;
import io.dropwizard.logging.layout.LayoutFactory;

import javax.annotation.Nullable;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.PrintStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

@JsonTypeName("default")
public class DefaultLoggingFactory implements LoggingFactory {
    private static final ReentrantLock MBEAN_REGISTRATION_LOCK = new ReentrantLock();
    private static final ReentrantLock CHANGE_LOGGER_CONTEXT_LOCK = new ReentrantLock();

    @NotNull
    private String level = "INFO";

    @NotNull
    private ImmutableMap<String, JsonNode> loggers = ImmutableMap.of();

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory<ILoggingEvent>> appenders = ImmutableList.of(
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
    public String getLevel() {
        return level;
    }

    @JsonProperty
    public void setLevel(String level) {
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

    @Override
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

    @Override
    public void stop() {
        // Should acquire the lock to avoid concurrent listener changes
        CHANGE_LOGGER_CONTEXT_LOCK.lock();
        try {
            // We need to go through a list of appenders and locate the async ones,
            // as those could have messages left to write. Since there is no flushing
            // mechanism built into logback, we wait for a short period of time before
            // giving up that the appender will be completely flushed.
            final Logger logger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            final ArrayList<Appender<ILoggingEvent>> appenders = Lists.newArrayList(logger.iteratorForAppenders());
            for (Appender<ILoggingEvent> appender : appenders) {
                if (appender instanceof AsyncAppender) {
                    flushAppender((AsyncAppender) appender);
                }
            }
        } catch (InterruptedException ignored) {
            // If the thread waiting for the logs to be flushed is aborted then
            // user clearly wants the application to quit now, so stop trying
            // to flush any appenders
            Thread.currentThread().interrupt();
        } finally {
            CHANGE_LOGGER_CONTEXT_LOCK.unlock();
        }
    }

    @Override
    public void reset() {
        CHANGE_LOGGER_CONTEXT_LOCK.lock();
        try {
            // Flush all the loggers and reinstate only the console logger as a
            // sane default.
            loggerContext.stop();
            final Logger logger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            logger.detachAndStopAllAppenders();
            final DropwizardLayout formatter = new DropwizardLayout(loggerContext, TimeZone.getDefault());
            formatter.start();
            final LayoutWrappingEncoder<ILoggingEvent> layoutEncoder = new LayoutWrappingEncoder<>();
            layoutEncoder.setLayout(formatter);
            final ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
            consoleAppender.addFilter(new ThresholdLevelFilterFactory().build(Level.INFO));
            consoleAppender.setEncoder(layoutEncoder);
            consoleAppender.setContext(loggerContext);
            consoleAppender.start();
            logger.addAppender(consoleAppender);
            loggerContext.start();
        } finally {
            CHANGE_LOGGER_CONTEXT_LOCK.unlock();
        }
    }

    private void flushAppender(AsyncAppender appender) throws InterruptedException {
        int timeWaiting = 0;
        while (timeWaiting < appender.getMaxFlushTime() && appender.getNumberOfElementsInQueue() > 0) {
            Thread.sleep(100);
            timeWaiting += 100;
        }

        if (appender.getNumberOfElementsInQueue() > 0) {
            // It may seem odd to log when we're trying to flush a logger that
            // isn't flushing, but the same warning is issued inside
            // appender.stop() if the appender isn't able to flush.
            appender.addWarn(appender.getNumberOfElementsInQueue() + " events may be discarded");
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

        root.setLevel(toLevel(level));

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
                logger.setLevel(toLevel(configuration.getLevel()));
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

    static Level toLevel(@Nullable String text) {
        // required because YAML maps "off" to a boolean false
        if ("false".equalsIgnoreCase(text)) {
            // required because YAML maps "off" to a boolean false
            return Level.OFF;
        } else if ("true".equalsIgnoreCase(text)) {
            // required because YAML maps "on" to a boolean true
            return Level.ALL;
        }
        return Level.toLevel(text, Level.INFO);
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
