package io.dropwizard.jersey.validation;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path.Node;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.Set;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
    @Override
    public Response toResponse(ConstraintViolationException exception) {
        int status = 422;

        // Detect where the constraint validation occurred so we can return an appropriate status
        // code. If the constraint failed with a *Param annotation, return a bad request. If it
        // failed validating the return value, return internal error. Else return unprocessable
        // entity.
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        if (violations.size() > 0) {
            ConstraintViolation<?> violation = violations.iterator().next();
            boolean isReturnValue = false;
            boolean isArgument = false;

            // A return value can only occur at the last path, but a parameter
            // can occur anywhere, such as a @BeanParam that has validations.
            for (Node node : violation.getPropertyPath()) {
                isArgument |= node.getKind().equals(ElementKind.PARAMETER);
                isReturnValue = node.getKind().equals(ElementKind.RETURN_VALUE);
            }

            if (isReturnValue) {
                status = 500;
            } else if (isArgument) {
                status = 400;
            }
        }

        final ValidationErrorMessage message = new ValidationErrorMessage(exception.getConstraintViolations());
        return Response.status(status)
                       .entity(message)
                       .build();
    }
}
