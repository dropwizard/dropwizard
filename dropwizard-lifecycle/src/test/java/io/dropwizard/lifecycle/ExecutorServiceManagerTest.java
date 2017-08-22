package io.dropwizard.lifecycle;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.concurrent.ExecutorService;

import org.junit.Test;

import io.dropwizard.util.Duration;

public class ExecutorServiceManagerTest {

    @Test
    public void testAccessors() {
        // This test verifies the accessors behave as advertised for other unit
        // tests.
        final Duration d = Duration.seconds(1L);
        final String poolName = this.getClass().getSimpleName();
        final ExecutorService exec = mock(ExecutorService.class);

        final ExecutorServiceManager test = new ExecutorServiceManager(exec, d, poolName);

        assertSame(d, test.getShutdownPeriod());
        assertSame(poolName, test.getPoolName());
        assertSame(exec, test.getExecutor());
    }

    @Test
    public void testManaged() throws Exception {
        final Duration d = Duration.seconds(1L);
        final String poolName = this.getClass().getSimpleName();
        final ExecutorService exec = mock(ExecutorService.class);

        final ExecutorServiceManager test = new ExecutorServiceManager(exec, d, poolName);

        test.start();

        verifyZeroInteractions(exec);

        test.stop();

        verify(exec, times(1)).shutdown();
        verify(exec, times(1)).awaitTermination(d.getQuantity(), d.getUnit());
    }
}
