package com.yammer.dropwizard.jdbi.jersey;

import com.yammer.dropwizard.jersey.LoggingExceptionMapper;
import com.yammer.dropwizard.logging.Log;
import org.skife.jdbi.v2.exceptions.DBIException;

import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

/**
 * Iterates through a DBIException's cause if it's a SQLException otherwise log as normal.
 */
@Provider
public class LoggingDBIExceptionMapper extends LoggingExceptionMapper<DBIException> {
    private static final Log LOG = Log.forClass(LoggingDBIExceptionMapper.class);

    @Override
    protected void logException(long id, DBIException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof SQLException) {
            for (Throwable throwable : (SQLException)cause) {
                LOG.error(throwable, formatLogMessage(id, throwable));
            }
        } else {
            LOG.error(exception, formatLogMessage(id, exception));
        }
    }
}
