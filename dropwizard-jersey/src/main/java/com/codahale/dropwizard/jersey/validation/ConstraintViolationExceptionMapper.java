package com.codahale.dropwizard.jersey.validation;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        final ValidationErrorMessage message = new ValidationErrorMessage(exception.getConstraintViolations());

        return Response.status(UNPROCESSABLE_ENTITY)
                       .entity(message)
                       .build();
    }
}
