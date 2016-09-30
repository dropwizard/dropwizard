package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import com.google.common.base.Throwables;
import io.dropwizard.jersey.errors.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.regex.Pattern;

@Provider
public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    // Pattern to match jackson error messages where a class lacks a single argument constructor
    // or factory to handle a given type. For example:
    // "no boolean/Boolean-argument constructor/factory method to deserialize from boolean value"
    private static final Pattern WRONG_TYPE_REGEX = Pattern.compile("factory method to deserialize from \\w+ value");

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);
    private final boolean showDetails;

    public JsonProcessingExceptionMapper() {
        this(false);
    }

    public JsonProcessingExceptionMapper(boolean showDetails) {
        this.showDetails = showDetails;
    }

    public boolean isShowDetails() {
        return showDetails;
    }

    @Override
    public Response toResponse(JsonProcessingException exception) {
        /*
         * If the error is in the JSON generation, it's a server error.
         */
        if (exception instanceof JsonGenerationException) {
            LOGGER.warn("Error generating JSON", exception);
            return Response.serverError().build();
        }

        final String message = exception.getOriginalMessage();

        /*
         * If we can't deserialize the JSON because someone forgot a no-arg
         * constructor, or it is not known how to serialize the type it's
         * a server error and we should inform the developer.
         */
        if (exception instanceof JsonMappingException) {
            final JsonMappingException ex = (JsonMappingException) exception;
            final Throwable cause = Throwables.getRootCause(ex);

            // Exceptions that denote an error on the client side
            final boolean clientCause = cause instanceof InvalidFormatException ||
                cause instanceof PropertyBindingException;

            // Until completely foolproof mechanism can be worked out in coordination
            // with Jackson on how to communicate client vs server fault, compare
            // start of message with known server faults.
            final boolean beanError = cause.getMessage().startsWith("No suitable constructor found") ||
                cause.getMessage().startsWith("No serializer found for class") ||
                (cause.getMessage().startsWith("Can not construct instance") &&
                    !WRONG_TYPE_REGEX.matcher(cause.getMessage()).find());

            if (beanError && !clientCause) {
                LOGGER.error("Unable to serialize or deserialize the specific type", exception);
                return Response.serverError().build();
            }
        }

        /*
         * Otherwise, it's those pesky users.
         */
        LOGGER.debug("Unable to process JSON", exception);
        final ErrorMessage errorMessage = new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(),
                "Unable to process JSON", showDetails ? message : null);
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(errorMessage)
                .build();
    }
}
