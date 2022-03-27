package io.dropwizard.lifecycle;

import io.dropwizard.util.Duration;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExecutorServiceManagerTest {

    private static final Duration TEST_DURATION = Duration.seconds(1L);
    private final ExecutorService exec = mock(ExecutorService.class);

    @Test
    void testAccessors() {
        // This test verifies the accessors behave as advertised for other unit
        // tests.
        final String poolName = this.getClass().getSimpleName();

        final ExecutorServiceManager test = new ExecutorServiceManager(this.exec, TEST_DURATION, poolName);

        assertThat(test.getShutdownPeriod()).isSameAs(TEST_DURATION);
        assertThat(test.getPoolName()).isSameAs(poolName);
        assertThat(test.getExecutor()).isSameAs(this.exec);
        assertThat(test.toString()).contains(String.format("(%s)", poolName));
    }

    @Test
    void testManaged() throws Exception {
        final String poolName = this.getClass().getSimpleName();
        when(this.exec.awaitTermination(anyLong(), any())).thenReturn(true);

        final ExecutorServiceManager test = new ExecutorServiceManager(this.exec, TEST_DURATION, poolName);

        test.start();

        verifyNoInteractions(this.exec);

        test.stop();

        verify(this.exec).shutdown();
        verify(this.exec).awaitTermination(TEST_DURATION.getQuantity(), TEST_DURATION.getUnit());
    }

    @Test
    void testManagedTimeout() throws Exception {
        final String poolName = this.getClass().getSimpleName();
        when(this.exec.awaitTermination(anyLong(), any())).thenReturn(false);

        final ExecutorServiceManager test = new ExecutorServiceManager(this.exec, TEST_DURATION, poolName);

        test.start();

        verifyNoInteractions(this.exec);

        test.stop();

        verify(this.exec).shutdown();
        verify(this.exec).awaitTermination(TEST_DURATION.getQuantity(), TEST_DURATION.getUnit());
    }
}
