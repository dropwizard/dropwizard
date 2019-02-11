package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.annotation.Nullable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScheduledExecutorServiceBuilderTest {

    private static final Duration DEFAULT_SHUTDOWN_PERIOD = Duration.seconds(5L);

    private final LifecycleEnvironment le;

    @Nullable
    private ScheduledExecutorService execTracker;

    public ScheduledExecutorServiceBuilderTest() {
        this.execTracker = null;
        this.le = mock(LifecycleEnvironment.class);
        when(le.getMetricRegistry()).thenReturn(new MetricRegistry());
    }

    @After
    public void tearDown() {
        if (this.execTracker != null) {
            this.execTracker.shutdownNow();

            try {
                this.execTracker.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            } catch (final InterruptedException e) {
                // Reset the interrupted state of the thread as throwing an
                // InterruptedException clears the state. Since we are
                // swallowing the exception, we have to re-interrupt the thread.
                Thread.currentThread().interrupt();
            }

            this.execTracker = null;
        }
    }

    @Test
    public void testBasicInvocation() {
        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(this.le,
            poolName,
            false);

        this.execTracker = test.build();
        assertThat(this.execTracker).isInstanceOf(InstrumentedScheduledExecutorService.class);

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isInstanceOf(ScheduledThreadPoolExecutor.class);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }
}
