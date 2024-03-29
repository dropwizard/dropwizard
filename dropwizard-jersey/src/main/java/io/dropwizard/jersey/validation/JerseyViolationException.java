package io.dropwizard.jersey.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.glassfish.jersey.server.model.Invocable;

import java.util.Set;

/**
 * A {@link ConstraintViolationException} that occurs while Jersey is
 * validating constraints on a resource endpoint.
 */
public class JerseyViolationException extends ConstraintViolationException {
    private static final long serialVersionUID = -2084629736062306666L;
    private final Invocable invocable;

    public JerseyViolationException(Set<? extends ConstraintViolation<?>> constraintViolations, Invocable invocable) {
        super(constraintViolations);
        this.invocable = invocable;
    }

    public Invocable getInvocable() {
        return invocable;
    }
}
