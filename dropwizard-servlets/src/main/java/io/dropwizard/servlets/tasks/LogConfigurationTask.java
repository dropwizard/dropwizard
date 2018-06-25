package io.dropwizard.servlets.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
    public void execute(Map<String, List<String>> parameters, PrintWriter output) throws Exception {
        final List<String> loggerNames = getLoggerNames(parameters);
        final Level loggerLevel = getLoggerLevel(parameters);

        for (String loggerName : loggerNames) {
            ((LoggerContext) loggerContext).getLogger(loggerName).setLevel(loggerLevel);
            output.println(String.format("Configured logging level for %s to %s", loggerName, loggerLevel));
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
}
