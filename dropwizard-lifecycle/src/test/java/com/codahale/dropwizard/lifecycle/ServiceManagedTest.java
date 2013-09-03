package com.codahale.dropwizard.lifecycle;

import org.junit.Test;
import org.mockito.InOrder;

import com.google.common.util.concurrent.Service.State;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class ServiceManagedTest {
    private final Managed managed = mock(Managed.class);
    private final ServiceManaged jettyManaged = new ServiceManaged(managed);

    @Test
    public void startsAndStops() throws Exception {
        jettyManaged.starting();
        jettyManaged.stopping(State.RUNNING);

        final InOrder inOrder = inOrder(managed);
        inOrder.verify(managed).start();
        inOrder.verify(managed).stop();
    }
}
