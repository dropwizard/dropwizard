package io.dropwizard.jdbi3.jersey;

import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jdbi.v3.core.JdbiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Iterates through a {@link JdbiException}'s cause if it's a {@link SQLException} otherwise log as normal.
 */
@Provider
public class LoggingJdbiExceptionMapper extends LoggingExceptionMapper<JdbiException> {
    /**
     * @since 2.0
     */
    public LoggingJdbiExceptionMapper() {
        this(LoggerFactory.getLogger(LoggingJdbiExceptionMapper.class));
    }

    /**
     * @since 2.0
     */
    LoggingJdbiExceptionMapper(Logger logger) {
        super(logger);
    }

    @Override
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    protected void logException(long id, JdbiException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof SQLException sqlException) {
            for (Throwable throwable : sqlException) {
                logger.error(formatLogMessage(id, throwable), throwable);
            }
        } else {
            logger.error(formatLogMessage(id, exception), exception);
        }
    }
}
