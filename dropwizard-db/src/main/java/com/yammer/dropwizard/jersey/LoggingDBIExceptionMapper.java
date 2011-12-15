package com.yammer.dropwizard.jersey;

import org.skife.jdbi.v2.exceptions.DBIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.sql.SQLException;
import java.util.Random;

/**
 * Iterates through a DBIException's cause if it's a SQLException otherwise log as normal.
 */
@Provider
public class LoggingDBIExceptionMapper implements ExceptionMapper<DBIException> {
    private final Logger logger = LoggerFactory.getLogger(LoggingDBIExceptionMapper.class);
    private final Random random = new Random();

    @Override
    public Response toResponse(DBIException exception) {
        final Throwable cause = exception.getCause();
        final long id = random.nextLong();

        if (cause instanceof SQLException) {
            for (Throwable throwable : (SQLException)cause)
                logger.error(String.format("Error handling a request: %016x", id), throwable);
        }
        else
            logger.error(String.format("Error handling a request: %016x", id), exception);


        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity(String.format(
                        "There was an error processing your request. It has been logged (ID %016x).\n",
                        id))
                .build();
    }
}
