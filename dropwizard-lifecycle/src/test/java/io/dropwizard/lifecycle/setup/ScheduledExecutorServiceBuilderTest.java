package io.dropwizard.lifecycle.setup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;

public class ScheduledExecutorServiceBuilderTest {

    private static final Duration DEFAULT_SHUTDOWN_PERIOD = Duration.seconds(5L);

    private final LifecycleEnvironment le;
    private ScheduledExecutorService execTracker;

    public ScheduledExecutorServiceBuilderTest() {
        this.execTracker = null;
        this.le = mock(LifecycleEnvironment.class);
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
        assertThat(this.execTracker).isInstanceOf(ScheduledThreadPoolExecutor.class);

        final ScheduledThreadPoolExecutor castedExec = (ScheduledThreadPoolExecutor) this.execTracker;
        assertThat(castedExec.getRemoveOnCancelPolicy()).isFalse();

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isSameAs(this.execTracker);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }

    @Test
    public void testRemoveOnCancelTrue() {
        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(this.le,
            poolName,
            false);

        this.execTracker = test.removeOnCancelPolicy(true).build();
        assertThat(this.execTracker).isInstanceOf(ScheduledThreadPoolExecutor.class);

        final ScheduledThreadPoolExecutor castedExec = (ScheduledThreadPoolExecutor) this.execTracker;
        assertThat(castedExec.getRemoveOnCancelPolicy()).isTrue();

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isSameAs(this.execTracker);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }

    @Test
    public void testRemoveOnCancelFalse() {
        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(this.le,
            poolName,
            false);

        this.execTracker = test.removeOnCancelPolicy(false).build();
        assertThat(this.execTracker).isInstanceOf(ScheduledThreadPoolExecutor.class);

        final ScheduledThreadPoolExecutor castedExec = (ScheduledThreadPoolExecutor) this.execTracker;
        assertThat(castedExec.getRemoveOnCancelPolicy()).isFalse();

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isSameAs(this.execTracker);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }

    @Test
    public void testPredefinedThreadFactory() {
        final ThreadFactory tfactory = mock(ThreadFactory.class);
        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(this.le,
            poolName,
            tfactory);

        this.execTracker = test.removeOnCancelPolicy(false).build();
        assertThat(this.execTracker).isInstanceOf(ScheduledThreadPoolExecutor.class);

        final ScheduledThreadPoolExecutor castedExec = (ScheduledThreadPoolExecutor) this.execTracker;
        assertThat(castedExec.getRemoveOnCancelPolicy()).isFalse();
        assertThat(castedExec.getThreadFactory()).isSameAs(tfactory);

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(this.le).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertThat(esmCaptured.getExecutor()).isSameAs(this.execTracker);
        assertThat(esmCaptured.getShutdownPeriod()).isEqualTo(DEFAULT_SHUTDOWN_PERIOD);
        assertThat(esmCaptured.getPoolName()).isSameAs(poolName);
    }
}
