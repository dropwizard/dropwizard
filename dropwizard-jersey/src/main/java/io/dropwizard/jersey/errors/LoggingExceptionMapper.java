package io.dropwizard.jersey.errors;

import io.dropwizard.util.RequestId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.concurrent.ThreadLocalRandom;

public abstract class LoggingExceptionMapper<E extends Throwable> implements ExceptionMapper<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingExceptionMapper.class);

    @Override
    public Response toResponse(E exception) {
        if (exception instanceof WebApplicationException) {
            return ((WebApplicationException) exception).getResponse();
        }

        final long id = getServiceRequestId();
        logException(id, exception);
        return Response.serverError()
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

    /**
     * @return service request id from the underlying logger MDC, or a random number if not available.
     */
    long getServiceRequestId() {
        String requestId = MDC.get(RequestId.SERVICE_REQUEST_ID);
        if (requestId != null) {
            return Long.valueOf(requestId);
        }

        return ThreadLocalRandom.current().nextLong();
    }
}
