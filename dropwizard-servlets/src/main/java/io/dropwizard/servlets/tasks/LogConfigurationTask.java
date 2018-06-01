package io.dropwizard.servlets.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.collect.ImmutableMultimap;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
 * </table>
 * </p>
 */
public class LogConfigurationTask extends Task {

    private final ILoggerFactory loggerContext;
    private final Timer timer = new Timer(LogConfigurationTask.class.getSimpleName(), true);

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
        super("log-level");
        this.loggerContext = loggerContext;
    }

    @Override
    public void execute(ImmutableMultimap<String, String> parameters, PrintWriter output) throws Exception {
        final List<String> loggerNames = getLoggerNames(parameters);
        final Level loggerLevel = getLoggerLevel(parameters);
        final Duration duration = getDuration(parameters);

        for (String loggerName : loggerNames) {
            Logger logger = ((LoggerContext) loggerContext).getLogger(loggerName);

            String message = String.format("Configured logging level for %s to %s", loggerName, loggerLevel);

            if (loggerLevel != null && duration != null) {
                // Get the effective level before setting the new level
                final Level effectiveLevel = logger.getEffectiveLevel();
                final long millis = duration.toMillis();
                timer.schedule(new TimerTask() {
                    public void run() {
                        logger.setLevel(effectiveLevel);
                    }
                }, millis);

                message += String.format(" for %s milliseconds", millis);
            }

            logger.setLevel(loggerLevel);
            output.println(message);
            output.flush();
        }
    }

    private List<String> getLoggerNames(ImmutableMultimap<String, String> parameters) {
        return parameters.get("logger").asList();
    }

    @Nullable
    private Level getLoggerLevel(ImmutableMultimap<String, String> parameters) {
        final List<String> loggerLevels = parameters.get("level").asList();
        return loggerLevels.isEmpty() ? null : Level.valueOf(loggerLevels.get(0));
    }

    private Duration getDuration(ImmutableMultimap<String, String> parameters) {
        final List<String> durations = parameters.get("duration").asList();
        return durations.isEmpty() ? null : Duration.parse(durations.get(0));
    }
}
