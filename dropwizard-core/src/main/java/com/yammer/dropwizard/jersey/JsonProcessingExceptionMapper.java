package com.yammer.dropwizard.jersey;

import org.codehaus.jackson.JsonProcessingException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class JsonProcessingExceptionMapper implements ExceptionMapper<JsonProcessingException> {
    @Override
    public Response toResponse(JsonProcessingException exception) {
        throw new WebApplicationException(exception, Response.Status.BAD_REQUEST);
    }
}
