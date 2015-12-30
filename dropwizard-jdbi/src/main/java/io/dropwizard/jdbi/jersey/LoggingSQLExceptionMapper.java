package io.dropwizard.jdbi.jersey;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

/**
 * Iterates through SQLExceptions to log all causes
 */
@Provider
public class LoggingSQLExceptionMapper extends LoggingExceptionMapper<SQLException> {
    private static Logger logger = LoggerFactory.getLogger(LoggingSQLExceptionMapper.class);

    @Override
    protected void logException(long id, SQLException exception) {
        final String message = formatLogMessage(id, exception);
        for (Throwable throwable : exception) {
            logger.error(message, throwable);
        }
    }

    @VisibleForTesting
    static synchronized void setLogger(Logger newLogger) {
        logger = newLogger;
    }
}
