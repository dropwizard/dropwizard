package com.yammer.dropwizard.jersey;

import com.yammer.dropwizard.logging.Log;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Random;

// TODO: 10/12/11 <coda> -- write tests for LoggingExceptionMapper
// TODO: 10/12/11 <coda> -- write docs for LoggingExceptionMapper

@Provider
public class LoggingExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {
    private static final Log LOG = Log.forClass(LoggingExceptionMapper.class);
    private static final Random RANDOM = new Random();

    @Override
    public Response toResponse(E exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }
        final long id = randomId();
        logException(id, exception);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .type(MediaType.TEXT_PLAIN_TYPE)
                       .entity(formatResponseEntity(id, exception))
                       .build();
    }

    protected void logException(long id, E exception) {
        LOG.error(exception, formatLogMessage(id, exception));
    }

    protected String formatResponseEntity(long id, Throwable exception) {
        return String.format("There was an error processing your request. It has been logged (ID %016x).\n", id);
    }

    protected String formatLogMessage(long id, Throwable exception) {
        return String.format("Error handling a request: %016x", id);
    }

    protected static long randomId() {
        return RANDOM.nextLong();
    }
}
