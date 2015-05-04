package io.dropwizard.jersey.validation;

import io.dropwizard.validation.ConstraintViolations;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        final ValidationErrorMessage message = new ValidationErrorMessage(exception.getConstraintViolations());
        return Response.status(ConstraintViolations.determineStatus(exception.getConstraintViolations()))
                       .entity(message)
                       .build();
    }
}
