package io.dropwizard.jdbi.jersey;

import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import org.skife.jdbi.v2.exceptions.DBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

/**
 * Iterates through a DBIException's cause if it's a SQLException otherwise log as normal.
 */
@Provider
public class LoggingDBIExceptionMapper extends LoggingExceptionMapper<DBIException> {
    public LoggingDBIExceptionMapper() {
        this(LoggerFactory.getLogger(LoggingDBIExceptionMapper.class));
    }

    LoggingDBIExceptionMapper(Logger logger) {
        super(logger);
    }

    @Override
    protected void logException(long id, DBIException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof SQLException) {
            for (Throwable throwable : (SQLException) cause) {
                logger.error(formatLogMessage(id, throwable), throwable);
            }
        } else {
            logger.error(formatLogMessage(id, exception), exception);
        }
    }
}
