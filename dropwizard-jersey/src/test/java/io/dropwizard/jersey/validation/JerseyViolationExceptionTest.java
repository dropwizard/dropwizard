package io.dropwizard.jersey.validation;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Invocable;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Request;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class JerseyViolationExceptionTest {

    @Test
    public void testAccessors() {
        final Set<? extends ConstraintViolation<?>> violations = Collections.emptySet();

        @SuppressWarnings("unchecked")
        final Inflector<Request, ?> inf = mock(Inflector.class);
        final Invocable inv = Invocable.create(inf);
        final JerseyViolationException test = new JerseyViolationException(violations, inv);
        assertSame(inv, test.getInvocable());
    }
}
