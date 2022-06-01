package io.dropwizard.lifecycle;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class JettyManagedTest {
    private final Managed managed = mock(Managed.class);
    private final JettyManaged jettyManaged = new JettyManaged(managed);

    @Test
    void startsAndStops() throws Exception {
        jettyManaged.start();
        jettyManaged.stop();

        final InOrder inOrder = inOrder(managed);
        inOrder.verify(managed).start();
        inOrder.verify(managed).stop();
    }
}
