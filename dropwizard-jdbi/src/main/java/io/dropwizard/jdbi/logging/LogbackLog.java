package io.dropwizard.jdbi.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.logging.FormattedLog;
import org.slf4j.LoggerFactory;

/**
 * Logs SQL via Logback
 */
public class LogbackLog extends FormattedLog {
    private final Logger log;
    private final Level level;
    private final String fqcn;

    /**
     * Logs to org.skife.jdbi.v2 logger at the debug level
     */
    public LogbackLog() {
        this((Logger) LoggerFactory.getLogger(DBI.class.getPackage().getName()));
    }

    /**
     * Use an arbitrary logger to log to at the debug level
     */
    public LogbackLog(Logger log) {
        this(log, Level.DEBUG);
    }

    /**
     * Specify both the logger and the level to log at
     * @param log The logger to log to
     * @param level the priority to log at
     */
    public LogbackLog(Logger log, Level level) {
        this.log = log;
        this.level = level;
        this.fqcn = LogbackLog.class.getName();
    }

    @Override
    protected final boolean isEnabled() {
        return log.isEnabledFor(level);
    }

    @Override
    protected final void log(String msg) {
        log.log(null, fqcn, Level.toLocationAwareLoggerInteger(level), msg, null, null);
    }
}
