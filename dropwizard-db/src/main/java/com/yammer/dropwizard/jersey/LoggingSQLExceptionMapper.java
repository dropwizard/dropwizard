package com.yammer.dropwizard.jersey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.sql.SQLException;
import java.util.Random;

/**
 * Iterates through SQLExceptions to log all causes
 */
@Provider
public class LoggingSQLExceptionMapper implements ExceptionMapper<SQLException> {
    private final Logger logger = LoggerFactory.getLogger(LoggingSQLExceptionMapper.class);
    private final Random random = new Random();

    @Override
    public Response toResponse(SQLException exception) {
        final long id = random.nextLong();

        for (Throwable throwable : exception)
            logger.error(String.format("Error handling a request: %016x", id), throwable);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity(String.format(
                        "There was an error processing your request. It has been logged (ID %016x).\n",
                        id))
                .build();
    }
}
