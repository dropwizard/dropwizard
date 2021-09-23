package io.dropwizard.health;

import org.eclipse.jetty.util.thread.ShutdownThread;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DelayedShutdownHandlerTest {
    private final ShutdownNotifier shutdownNotifier = mock(ShutdownNotifier.class);
    private final DelayedShutdownHandler delayedShutdownHandler = new DelayedShutdownHandler(shutdownNotifier);

    @Test
    void testMethods() throws Exception {
        delayedShutdownHandler.register();
        assertThat(ShutdownThread.isRegistered(delayedShutdownHandler)).isTrue();

        delayedShutdownHandler.doStop();
        verify(shutdownNotifier).notifyShutdownStarted();
    }
}
