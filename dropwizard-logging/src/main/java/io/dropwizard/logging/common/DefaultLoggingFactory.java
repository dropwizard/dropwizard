package io.dropwizard.logging.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AsyncAppenderBase;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logback.AsyncAppenderBaseProxy;
import io.dropwizard.logging.common.async.AsyncAppenderFactory;
import io.dropwizard.logging.common.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.common.filter.LevelFilterFactory;
import io.dropwizard.logging.common.filter.ThresholdLevelFilterFactory;
import io.dropwizard.logging.common.layout.DropwizardLayoutFactory;
import io.dropwizard.logging.common.layout.LayoutFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;

@JsonTypeName("default")
public class DefaultLoggingFactory implements LoggingFactory {
    private static final ReentrantLock CHANGE_LOGGER_CONTEXT_LOCK = new ReentrantLock();

    @NotNull
    private String level = "INFO";

    @NotNull
    private Map<String, JsonNode> loggers = Collections.emptyMap();

    @Valid
    @NotNull
    private List<AppenderFactory<ILoggingEvent>> appenders = Collections.singletonList(new ConsoleAppenderFactory<>());

    @JsonIgnore
    private final LoggerContext loggerContext;

    @JsonIgnore
    private final PrintStream configurationErrorsStream;

    public DefaultLoggingFactory() {
        this(LoggingUtil.getLoggerContext(), System.err);
    }

    DefaultLoggingFactory(LoggerContext loggerContext, PrintStream configurationErrorsStream) {
        super();
        this.loggerContext = requireNonNull(loggerContext);
        this.configurationErrorsStream = requireNonNull(configurationErrorsStream);
    }

    LoggerContext getLoggerContext() {
        return loggerContext;
    }

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
    public Map<String, JsonNode> getLoggers() {
        return loggers;
    }

    @JsonProperty
    public void setLoggers(Map<String, JsonNode> loggers) {
        this.loggers = new HashMap<>(loggers);
    }

    @JsonProperty
    public List<AppenderFactory<ILoggingEvent>> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(List<AppenderFactory<ILoggingEvent>> appenders) {
        this.appenders = new ArrayList<>(appenders);
    }

    @Override
    public void configure(MetricRegistry metricRegistry, String name) {
        LoggingUtil.hijackJDKLogging();

        final Logger root;
        CHANGE_LOGGER_CONTEXT_LOCK.lock();
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
            final Iterator<Appender<ILoggingEvent>> appenderIterator = logger.iteratorForAppenders();
            while (appenderIterator.hasNext()) {
                final Appender<ILoggingEvent> appender = appenderIterator.next();
                if (appender instanceof AsyncAppenderBase<?> asyncAppenderBase) {
                    flushAppender(asyncAppenderBase);
                } else if (appender instanceof AsyncAppenderBaseProxy<?> asyncAppenderBaseProxy) {
                    flushAppender(asyncAppenderBaseProxy.getAppender());
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

    private void flushAppender(AsyncAppenderBase<?> appender) throws InterruptedException {
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

        final ObjectMapper objectMapper = Jackson.newObjectMapper();

        for (Map.Entry<String, JsonNode> entry : loggers.entrySet()) {
            final Logger logger = loggerContext.getLogger(entry.getKey());
            final JsonNode jsonNode = entry.getValue();
            if (jsonNode.isTextual() || jsonNode.isBoolean()) {
                // Just a level as a string
                logger.setLevel(toLevel(jsonNode.asText()));
            } else if (jsonNode.isObject()) {
                // A level and an appender
                final LoggerConfiguration configuration;
                try {
                    configuration = objectMapper.treeToValue(jsonNode, LoggerConfiguration.class);
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
        return "DefaultLoggingFactory{"
                + "level=" + level
                + ", loggers=" + loggers
                + ", appenders=" + appenders
                + '}';
    }
}
