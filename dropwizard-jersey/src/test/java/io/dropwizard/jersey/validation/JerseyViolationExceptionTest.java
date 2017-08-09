package io.dropwizard.jersey.validation;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.ws.rs.core.Request;

import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.model.Invocable;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

public class JerseyViolationExceptionTest {

    @Test
    public void testAccessors() {
        final Set<? extends ConstraintViolation<?>> violations = ImmutableSet.of();

        @SuppressWarnings("unchecked")
        final Inflector<Request, ?> inf = mock(Inflector.class);
        final Invocable inv = Invocable.create(inf);
        final JerseyViolationException test = new JerseyViolationException(violations, inv);
        assertSame(inv, test.getInvocable());
    }
}
