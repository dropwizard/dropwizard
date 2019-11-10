package io.dropwizard.jdbi3.jersey;

import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

/**
 * Iterates through {@link SQLException}s to log all causes
 */
@Provider
public class LoggingSQLExceptionMapper extends LoggingExceptionMapper<SQLException> {
    /**
     * @since 2.0
     */
    LoggingSQLExceptionMapper(Logger logger) {
        super(logger);
    }

    /**
     * @since 2.0
     */
    public LoggingSQLExceptionMapper() {
        this(LoggerFactory.getLogger(LoggingSQLExceptionMapper.class));
    }

    @Override
    protected void logException(long id, SQLException exception) {
        final String message = formatLogMessage(id, exception);
        for (Throwable throwable : exception) {
            logger.error(message, throwable);
        }
    }
}
