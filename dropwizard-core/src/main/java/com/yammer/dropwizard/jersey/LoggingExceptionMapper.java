package com.yammer.dropwizard.jersey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Random;

// TODO: 10/12/11 <coda> -- write tests for LoggingExceptionMapper
// TODO: 10/12/11 <coda> -- write docs for LoggingExceptionMapper

@Provider
public class LoggingExceptionMapper implements ExceptionMapper<Throwable> {
    private final Logger logger = LoggerFactory.getLogger(LoggingExceptionMapper.class);
    private final Random random = new Random();

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        final long id = random.nextLong();
        logger.error(String.format("Error handling a request: %016x", id), exception);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .type(MediaType.TEXT_PLAIN_TYPE)
                       .entity(String.format(
                               "There was an error processing your request. It has been logged (ID %016x).\n",
                               id))
                       .build();
    }
}
