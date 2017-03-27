package io.dropwizard.jersey.errors;

import org.glassfish.jersey.server.internal.LocalizationMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;

/**
 * {@link javax.ws.rs.ext.ExceptionMapper ExceptionMapper} for {@link IllegalStateException}.
 */
@Provider
public class IllegalStateExceptionMapper extends LoggingExceptionMapper<IllegalStateException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IllegalStateExceptionMapper.class);

    @Override
    public Response toResponse(final IllegalStateException exception) {
        final String message = exception.getMessage();

        if (LocalizationMessages.FORM_PARAM_CONTENT_TYPE_ERROR().equals(message)) {
            /*
             * If a POST request contains a Content-Type that is not application/x-www-form-urlencoded, Jersey throws
             * IllegalStateException with or without @Consumes. See: https://java.net/jira/browse/JERSEY-2636
             */
            // Logs exception with additional information for developers.
            LOGGER.debug("If the HTTP method is POST and using @FormParam in a resource method"
                + ", Content-Type should be application/x-www-form-urlencoded.", exception);
            // Returns the same response as if NotSupportedException was thrown.
            return createResponse(new NotSupportedException());
        }

        // LoggingExceptionMapper will log exception
        return super.toResponse(exception);
    }

    private Response createResponse(final WebApplicationException exception) {
        final ErrorMessage errorMessage = new ErrorMessage(exception.getResponse().getStatus(),
            exception.getLocalizedMessage());
        return Response.status(errorMessage.getCode())
            .type(APPLICATION_JSON_TYPE)
            .entity(errorMessage)
            .build();
    }
}
