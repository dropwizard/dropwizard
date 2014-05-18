package io.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.jmx.JMXConfigurator;
import ch.qos.logback.classic.jul.LevelChangePropagator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Layout;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.logback.InstrumentedAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.logging.filter.FilterFactory;
import io.dropwizard.logging.filter.ThresholdFilterFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.management.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * A factory for configuring logging.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code level}</td>
 *         <td>INFO</td>
 *         <td>The threshold below which logs will be discarded.</td>
 *     </tr>
 *     <td>
 *         <td>{@code timeZone}</td>
 *         <td>UTC</td>
 *         <td>The time zone to which timestamps will be converted if using the default {@code logFormat}.</td>
 *     </td>
 *     <tr>
 *         <td>{@code logFormat}</td>
 *         <td>%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx</td>
 *         <td>
 *             The Logback pattern with which events will be formatted. See
 *             <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
 *             for details.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code loggers}</td>
 *         <td></td>
 *         <td>A map of logger names to the log threshold for that logger.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code appenders}</td>
 *         <td>a default {@link ConsoleAppenderFactory console} appender</td>
 *         <td>The set of {@link AppenderFactory appenders} to which requests will be logged.</td>
 *     </tr>
 * </table>
 */
public class LoggingFactory {

    // initially configure for WARN+ console logging
    public static void bootstrap() {
        bootstrap(Level.WARN);
    }

    public static void bootstrap(Level level) {
        bootstrap(level, getDefaultLogFormat(TimeZone.getDefault()));
    }

    public static void bootstrap(Level level, String logFormat) {
        hijackJDKLogging();

        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        root.detachAndStopAllAppenders();

        final DropwizardLayout formatter = new DropwizardLayout(root.getLoggerContext(), logFormat);
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

    private static String getDefaultLogFormat(TimeZone timeZone) {
        return "%-5p [%d{ISO8601," + timeZone.getID() + "}] %c: %m%n%rEx";
    }

    @NotNull
    private Level level = Level.INFO;

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @NotNull
    private Optional<String> logFormat = Optional.absent();

    @NotNull
    private ImmutableMap<String, Level> loggers = ImmutableMap.of();

    @Valid
    @NotNull
    private ImmutableList<AppenderFactory<ILoggingEvent>> appenders = ImmutableList.<AppenderFactory<ILoggingEvent>>of(
            new ConsoleAppenderFactory<ILoggingEvent>()
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
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty
    public String getLogFormat() {
        return logFormat.or(getDefaultLogFormat(timeZone));
    }

    @JsonProperty
    public void setLogFormat(String logFormat) {
        this.logFormat = Optional.fromNullable(logFormat);
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
    public ImmutableList<AppenderFactory<ILoggingEvent>> getAppenders() {
        return appenders;
    }

    @JsonProperty
    public void setAppenders(List<AppenderFactory<ILoggingEvent>> appenders) {
        this.appenders = ImmutableList.copyOf(appenders);
    }

    public void configure(MetricRegistry metricRegistry, String name) {
        hijackJDKLogging();

        final Logger root = configureLevels();
        final LoggerContext context = root.getLoggerContext();

        final FilterFactory<ILoggingEvent> thresholdFilterFactory = new ThresholdFilterFactory();
        final AsyncAppenderFactory<ILoggingEvent> asyncAppenderFactory = new AsyncLoggingEventAppenderFactory();

        for (AppenderFactory<ILoggingEvent> output : appenders) {
            final Layout<ILoggingEvent> layout = new DropwizardLayout(context, getLogFormat());
            layout.start();
            root.addAppender(output.build(context, name, layout, thresholdFilterFactory, asyncAppenderFactory));
        }

        final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName objectName = new ObjectName("io.dropwizard:type=Logging");
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

    public void stop() {
        ILoggerFactory loggerFactory = LoggerFactory.getILoggerFactory();
        if (loggerFactory instanceof LoggerContext) {
            LoggerContext context = (LoggerContext) loggerFactory;
            context.stop();
        }
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
