package com.codahale.dropwizard.jersey.jackson;

import com.codahale.dropwizard.jersey.errors.ErrorMessage;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Iterator;

// TODO: 4/24/13 <coda> -- write tests for JsonProcessingExceptionMapper

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonProcessingExceptionMapper.class);
    private static final Splitter LINE_SPLITTER = Splitter.on("\n").trimResults();

    @Override
    public Response toResponse(JsonProcessingException exception) {
        /*
         * If the error is in the JSON generation, it's a server error.
         */
        if (exception instanceof JsonGenerationException) {
            LOGGER.warn("Error generating JSON", exception);
            return Response.serverError().build();
        }

        final String message = exception.getMessage();

        /*
         * If we can't deserialize the JSON because someone forgot a no-arg constructor, it's a
         * server error and we should inform the developer.
         */
        if (message.startsWith("No suitable constructor found")) {
            LOGGER.error("Unable to deserialize the specific type", exception);
            return Response.serverError().build();
        }

        /*
         * Otherwise, it's those pesky users.
         */
        LOGGER.debug("Unable to process JSON", exception);
        return Response.status(Response.Status.BAD_REQUEST)
                       .entity(new ErrorMessage(stripLocation(message)))
                       .build();
    }

    private String stripLocation(String message) {
        final Iterator<String> iterator = LINE_SPLITTER.split(message).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return message;
    }
}
