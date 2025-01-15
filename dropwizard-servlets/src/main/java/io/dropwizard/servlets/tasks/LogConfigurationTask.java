package io.dropwizard.servlets.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Sets the logging level for a number of loggers
 * <p>
 * <b>Parameters:</b>
 * <table>
 * <tr>
 * <td>Name</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>logger</td>
 * <td>One or more logger names to be configured with the specified log level.</td>
 * </tr>
 * <tr>
 * <td>level</td>
 * <td>An optional {@link Level} to configure. If not provided, the log level will be set to null.</td>
 * </tr>
 * </tr>
 * <tr>
 * <td>duration</td>
 * <td>An optional {@link Duration} to configure the level. If not provided, the log level will be set forever.</td>
 * </tr>
 * </table>
 * </p>
 */
public class LogConfigurationTask extends Task {

    private final ILoggerFactory loggerContext;
    private final Supplier<Timer> timerSupplier;
    private final AtomicReference<Timer> timerReference = new AtomicReference<>();

    /**
     * Creates a new LogConfigurationTask.
     */
    public LogConfigurationTask() {
        this(LoggerFactory.getILoggerFactory());
    }

    /**
     * Creates a new LogConfigurationTask with the given {@link ILoggerFactory} instance.
     * <p/>
     * <b>Use {@link LogConfigurationTask#LogConfigurationTask()} instead.</b>
     *
     * @param loggerContext a {@link ILoggerFactory} instance
     */
    public LogConfigurationTask(ILoggerFactory loggerContext) {
        this(loggerContext, () -> new Timer(LogConfigurationTask.class.getSimpleName(), true));
    }

    /**
     * Creates a new LogConfigurationTask with the given {@link ILoggerFactory} instance and a custom {@link Timer}.
     * <p/>
     * <b>Use {@link LogConfigurationTask#LogConfigurationTask()} instead.</b>
     *
     * @param loggerContext a {@link ILoggerFactory} instance
     * @param timerSupplier a {@link Supplier} supplying a {@link Timer}
     */
    LogConfigurationTask(ILoggerFactory loggerContext, Supplier<Timer> timerSupplier) {
        super("log-level");
        this.loggerContext = loggerContext;
        this.timerSupplier = timerSupplier;
    }

    @Override
    public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
        final List<String> loggerNames = getLoggerNames(parameters);
        final Level loggerLevel = getLoggerLevel(parameters);
        final Duration duration = getDuration(parameters);

        for (String loggerName : loggerNames) {
            Logger logger = ((LoggerContext) loggerContext).getLogger(loggerName);

            String message = String.format("Configured logging level for %s to %s", loggerName, loggerLevel);

            if (loggerLevel != null && duration != null) {
                final long millis = duration.toMillis();
                getTimer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        logger.setLevel(null);
                    }
                }, millis);

                message += String.format(" for %s milliseconds", millis);
            }

            logger.setLevel(loggerLevel);
            output.println(message);
            output.flush();
        }
    }

    private List<String> getLoggerNames(Map<String, List<String>> parameters) {
        return parameters.getOrDefault("logger", Collections.emptyList());
    }

    @Nullable
    private Level getLoggerLevel(Map<String, List<String>> parameters) {
        final List<String> loggerLevels = parameters.getOrDefault("level", Collections.emptyList());
        return loggerLevels.isEmpty() ? null : Level.valueOf(loggerLevels.get(0));
    }

    @Nullable
    private Duration getDuration(Map<String, List<String>> parameters) {
        final List<String> durations = parameters.getOrDefault("duration", Collections.emptyList());
        return durations.isEmpty() ? null : Duration.parse(durations.get(0));
    }

    /**
     * Lazy create the timer to avoid unnecessary thread creation unless an expirable log configuration task is submitted
     */
    @NonNull
    private Timer getTimer() {
        return timerReference.updateAndGet(timer -> timer == null ? timerSupplier.get() : timer);
    }
}
