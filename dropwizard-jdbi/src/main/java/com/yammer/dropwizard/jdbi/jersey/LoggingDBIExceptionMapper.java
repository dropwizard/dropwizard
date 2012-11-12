package com.yammer.dropwizard.jdbi.jersey;

import com.yammer.dropwizard.jersey.LoggingExceptionMapper;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingDBIExceptionMapper.class);

    @Override
    protected void logException(long id, DBIException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof SQLException) {
            for (Throwable throwable : (SQLException)cause) {
                LOGGER.error(formatLogMessage(id, throwable), throwable);
            }
        } else {
            LOGGER.error(formatLogMessage(id, exception), exception);
        }
    }
}
