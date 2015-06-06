package io.dropwizard.jersey.validation;

import org.glassfish.jersey.server.model.Invocable;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

/**
 * A {@link ConstraintViolationException} that occurs while Jersey is
 * validating constraints on a resource endpoint.
 */
public class JerseyViolationException extends ConstraintViolationException {
    private final Invocable invocable;

    public JerseyViolationException(Set<? extends ConstraintViolation<?>> constraintViolations, Invocable invocable) {
        super(constraintViolations);
        this.invocable = invocable;
    }

    public Invocable getInvocable() {
        return invocable;
    }
}
