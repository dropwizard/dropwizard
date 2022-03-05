package io.dropwizard.jersey.validation;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Invocable;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.ws.rs.core.Request;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class JerseyViolationExceptionTest {

    @Test
    void testAccessors() {
        final Set<? extends ConstraintViolation<?>> violations = Collections.emptySet();

        @SuppressWarnings("unchecked")
        final Inflector<Request, ?> inf = mock(Inflector.class);
        final Invocable inv = Invocable.create(inf);
        final JerseyViolationException test = new JerseyViolationException(violations, inv);
        assertSame(inv, test.getInvocable());
    }
}
