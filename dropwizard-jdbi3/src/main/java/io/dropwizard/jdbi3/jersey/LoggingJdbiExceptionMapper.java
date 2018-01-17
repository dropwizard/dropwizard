package io.dropwizard.jdbi3.jersey;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

/**
 * Iterates through a {@link JdbiException}'s cause if it's a {@link SQLException} otherwise log as normal.
 */
@Provider
public class LoggingJdbiExceptionMapper extends LoggingExceptionMapper<JdbiException> {
    private static Logger logger = LoggerFactory.getLogger(LoggingJdbiExceptionMapper.class);

    @Override
    protected void logException(long id, JdbiException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof SQLException) {
            for (Throwable throwable : (SQLException) cause) {
                logger.error(formatLogMessage(id, throwable), throwable);
            }
        } else {
            logger.error(formatLogMessage(id, exception), exception);
        }
    }

    @VisibleForTesting
    static synchronized void setLogger(Logger newLogger) {
        logger = newLogger;
    }
}
