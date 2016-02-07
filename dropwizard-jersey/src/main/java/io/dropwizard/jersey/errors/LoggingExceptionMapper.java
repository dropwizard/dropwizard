package io.dropwizard.jersey.errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.concurrent.ThreadLocalRandom;

@Provider
public abstract class LoggingExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingExceptionMapper.class);

    @Override
    public Response toResponse(E exception) {
        final int status;
        final ErrorMessage errorMessage;

        if (exception instanceof WebApplicationException) {
            final Response response = ((WebApplicationException) exception).getResponse();
            Response.Status.Family family = response.getStatusInfo().getFamily();
            if (family.equals(Response.Status.Family.REDIRECTION)) {
                return response;
            }
            if (family.equals(Response.Status.Family.SERVER_ERROR)) {
                logException(exception);
            }
            status = response.getStatus();
            errorMessage = new ErrorMessage(status, exception.getLocalizedMessage());
        } else {
            final long id = logException(exception);
            status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            errorMessage = new ErrorMessage(formatErrorMessage(id, exception));
        }

        return Response.status(status)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(errorMessage)
                .build();
    }

    @SuppressWarnings("UnusedParameters")
    protected String formatErrorMessage(long id, E exception) {
        return String.format("There was an error processing your request. It has been logged (ID %016x).", id);
    }

    protected long logException(E exception) {
        final long id = ThreadLocalRandom.current().nextLong();
        logException(id, exception);
        return id;
    }

    protected void logException(long id, E exception) {
        LOGGER.error(formatLogMessage(id, exception), exception);
    }

    @SuppressWarnings("UnusedParameters")
    protected String formatLogMessage(long id, Throwable exception) {
        return String.format("Error handling a request: %016x", id);
    }
}
