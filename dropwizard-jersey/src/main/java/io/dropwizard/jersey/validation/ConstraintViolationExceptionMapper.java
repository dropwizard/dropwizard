package io.dropwizard.jersey.validation;

import com.google.common.collect.ImmutableList;
import io.dropwizard.validation.ConstraintViolations;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    private static final int UNPROCESSABLE_ENTITY = 422;

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        final ImmutableList<String> errors = ConstraintViolations.formatUntyped(exception.getConstraintViolations());
        final ValidationErrorMessage message = new ValidationErrorMessage(errors);
        return Response.status(UNPROCESSABLE_ENTITY)
                       .entity(message)
                       .build();
    }
}
