package io.dropwizard.jersey;

import com.fasterxml.jackson.annotation.JsonInclude;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.NotFoundException;
import java.net.URI;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A generic exception mapper that converts exception messages into JSON format
 *
 * @author bryan
 */
@Provider
public class UnhandledExceptionToJsonMapper implements ExceptionMapper<Exception> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnhandledExceptionToJsonMapper.class);

    private static final transient ObjectMapper MAPPER = new ObjectMapper();
    private static final String URI_NOT_FOUND = "URI not found";
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(final Exception exception) {
        LOGGER.error("Unhandled exception", exception);

        if (exception instanceof NotFoundException) {
            ResponseBuilder builder = Response.status(Status.NOT_FOUND)
                    .entity(notFoundJSON(((NotFoundException) exception).getNotFoundUri()))
                    .type(MediaType.APPLICATION_JSON);
            return builder.build();
        } else if (exception instanceof WebApplicationException) {
            WebApplicationException e = (WebApplicationException) exception;
            if (e.getResponse() != null) {
                return Response.fromResponse(e.getResponse())
                        .entity(defaultJSON(exception))
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            } else {
                ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity(defaultJSON(exception))
                        .type(MediaType.APPLICATION_JSON);
                return builder.build();
            }
        } else {
            ResponseBuilder builder = Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity(defaultJSON(exception))
                    .type(MediaType.APPLICATION_JSON);
            return builder.build();
        }
    }

    private String notFoundJSON(URI uri) {
        ErrorInfo errorInfo = new ErrorInfo(String.format("%s %s", uri, URI_NOT_FOUND));

        try {
            return MAPPER.writeValueAsString(errorInfo);
        } catch (JsonProcessingException e) {
            LOGGER.error("notFoundJSON() Failed to serialize error", e);
            return "{\"message\":\"An internal error occurred\"}";
        }
    }

    private String defaultJSON(final Exception exception) {
        ErrorInfo errorInfo;
        if (exception.getMessage() == null && exception instanceof WebApplicationException) {
            WebApplicationException e = (WebApplicationException) exception;
            if (e.getResponse() != null) {
                errorInfo = new ErrorInfo(HttpStatus.getMessage(e.getResponse().getStatus()));
            } else {
                return null;
            }
        } else {
            errorInfo = new ErrorInfo(exception.getMessage());
        }
        

        try {
            return MAPPER.writeValueAsString(errorInfo);
        } catch (JsonProcessingException e) {
            LOGGER.error("defaultJSON() Failed to serialize error", e);
            return "{\"message\":\"An internal error occurred\"}";
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private static class ErrorInfo {

        private String message;
        private ImmutableList<String> errors;

        private ErrorInfo(String message) {
            this.message = message;
        }

        private ErrorInfo(String message, ImmutableList<String> errors) {
            this.message = message;
            this.errors = errors;
        }

        /**
         * Get the value of errors
         *
         * @return the value of errors
         */
        public ImmutableList<String> getErrors() {
            return errors;
        }

        /**
         * Set the value of errors
         *
         * @param errors new value of errors
         */
        public void setErrors(ImmutableList<String> errors) {
            this.errors = errors;
        }

        /**
         * Get the value of message
         *
         * @return the value of message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Set the value of message
         *
         * @param message new value of message
         */
        public void setMessage(String message) {
            this.message = message;
        }

    }
}
