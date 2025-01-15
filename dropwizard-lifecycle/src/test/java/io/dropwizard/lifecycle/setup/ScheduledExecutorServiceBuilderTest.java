package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedThreadFactory;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScheduledExecutorServiceBuilderTest {

    private static final Duration DEFAULT_SHUTDOWN_PERIOD = Duration.seconds(5L);

    private final LifecycleEnvironment le;

    @Nullable
    private ScheduledExecutorService execTracker;

    public ScheduledExecutorServiceBuilderTest() {
        this.execTracker = null;
        this.le = mock(LifecycleEnvironment.class);
        when(le.getMetricRegistry()).thenReturn(new MetricRegistry());
    }

    @AfterEach
    void tearDown() {
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
    void testBasicInvocation() {
        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(this.le,
            poolName,
            false);

        this.execTracker = test.build();

        assertThat(this.execTracker)
            .isInstanceOfSatisfying(ScheduledThreadPoolExecutor.class, exe -> assertThat(exe)
                .satisfies(castedExec -> assertThat(castedExec.getRemoveOnCancelPolicy()).isFalse())
                .satisfies(castedExec -> assertThat(castedExec.getThreadFactory()).isInstanceOf(InstrumentedThreadFactory.class)));

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isSameAs(this.execTracker);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }

    @Test
    void testRemoveOnCancelTrue() {
        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(this.le,
            poolName,
            false);

        this.execTracker = test.removeOnCancelPolicy(true).build();
        assertThat(this.execTracker)
            .isInstanceOfSatisfying(ScheduledThreadPoolExecutor.class, executor ->
                assertThat(executor.getRemoveOnCancelPolicy()).isTrue());

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isSameAs(this.execTracker);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }

    @Test
    void testRemoveOnCancelFalse() {
        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(this.le,
            poolName,
            false);

        this.execTracker = test.removeOnCancelPolicy(false).build();
        assertThat(this.execTracker)
            .isInstanceOfSatisfying(ScheduledThreadPoolExecutor.class, executor ->
                assertThat(executor.getRemoveOnCancelPolicy()).isFalse());

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isSameAs(this.execTracker);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }

    @Test
    void testPredefinedThreadFactory() {
        final ThreadFactory tfactory = mock(ThreadFactory.class);
        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(this.le,
            poolName,
            tfactory);

        this.execTracker = test.removeOnCancelPolicy(false).build();
        assertThat(this.execTracker)
            .isInstanceOfSatisfying(ScheduledThreadPoolExecutor.class, exe -> assertThat(exe)
                .satisfies(castedExec -> assertThat(castedExec.getRemoveOnCancelPolicy()).isFalse())
                .satisfies(castedExec -> assertThat(castedExec.getThreadFactory()).isInstanceOf(InstrumentedThreadFactory.class)));

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isSameAs(this.execTracker);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }
}
