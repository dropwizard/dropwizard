package com.yammer.dropwizard.jdbi.jersey;

import com.yammer.dropwizard.jersey.LoggingExceptionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

/**
 * Iterates through SQLExceptions to log all causes
 */
@Provider
public class LoggingSQLExceptionMapper extends LoggingExceptionMapper<SQLException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingSQLExceptionMapper.class);

    @Override
    protected void logException(long id, SQLException exception) {
        final String message = formatLogMessage(id, exception);
        for (Throwable throwable : exception) {
            LOGGER.error(message, throwable);
        }
    }
}
