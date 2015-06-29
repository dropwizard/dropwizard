package io.dropwizard.jersey.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.concurrent.ThreadLocalRandom;

@Provider
@Priority(Integer.MAX_VALUE)
public abstract class LoggingExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingExceptionMapper.class);

    @Override
    public Response toResponse(E exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        final long id = ThreadLocalRandom.current().nextLong();
        logException(id, exception);
        return Response.serverError()
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(new ErrorMessage(formatErrorMessage(id, exception)))
                .build();
    }

    @SuppressWarnings("UnusedParameters")
    protected String formatErrorMessage(long id, E exception) {
        return String.format("There was an error processing your request. It has been logged (ID %016x).", id);
    }

    protected void logException(long id, E exception) {
        LOGGER.error(formatLogMessage(id, exception), exception);
    }

    @SuppressWarnings("UnusedParameters")
    protected String formatLogMessage(long id, Throwable exception) {
        return String.format("Error handling a request: %016x", id);
    }
}
