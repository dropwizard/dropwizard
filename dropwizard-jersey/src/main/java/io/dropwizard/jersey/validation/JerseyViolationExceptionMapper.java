package io.dropwizard.jersey.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.model.Invocable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Provider
public class JerseyViolationExceptionMapper implements ExceptionMapper<JerseyViolationException> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JerseyViolationExceptionMapper.class);

    @Override
    public Response toResponse(final JerseyViolationException exception) {
        // Provide a way to log if desired, Issue #2128, PR #2129
        LOGGER.debug("Object validation failure", exception);

        final Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        final Invocable invocable = exception.getInvocable();
        final List<String> errors = exception.getConstraintViolations().stream()
                .map(violation -> ConstraintMessage.getMessage(violation, invocable))
                .collect(Collectors.toList());

        final int status = ConstraintMessage.determineStatus(violations, invocable);
        return Response.status(status)
                .entity(new ValidationErrorMessage(errors))
                .build();
    }
}
