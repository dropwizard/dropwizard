package io.dropwizard.lifecycle;

import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class JettyManagedTest {
    private final Managed managed = mock(Managed.class);
    private final JettyManaged jettyManaged = new JettyManaged(managed);

    @Test
    public void startsAndStops() throws Exception {
        jettyManaged.start();
        jettyManaged.stop();

        final InOrder inOrder = inOrder(managed);
        inOrder.verify(managed).start();
        inOrder.verify(managed).stop();
    }
}
