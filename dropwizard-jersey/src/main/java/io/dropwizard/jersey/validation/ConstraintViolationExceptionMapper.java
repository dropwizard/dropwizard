package io.dropwizard.jersey.validation;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import io.dropwizard.validation.ConstraintViolations;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        ImmutableList<String> errors = FluentIterable.from(exception.getConstraintViolations())
                .transform(new Function<ConstraintViolation<?>, String>() {
                    @Override
                    public String apply(ConstraintViolation<?> v) {
                        return ConstraintMessage.getMessage(v);
                    }
                }).toList();

        if (errors.size() == 0) {
            errors = ImmutableList.of(Strings.nullToEmpty(exception.getMessage()));
        }

        return Response.status(ConstraintViolations.determineStatus(exception.getConstraintViolations()))
                .entity(new ValidationErrorMessage(errors))
                .build();
    }
}
