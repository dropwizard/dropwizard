package io.dropwizard.lifecycle.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.util.Duration;

public class ScheduledExecutorServiceBuilderTest {

    private static final Duration DEFAULT_SHUTDOWN_PERIOD = Duration.seconds(5L);

    private ScheduledExecutorService execTracker;

    public ScheduledExecutorServiceBuilderTest() {
        super();
        this.execTracker = null;
    }

    @After
    public void tearDown() {
        if (this.execTracker != null) {
            this.execTracker.shutdown();
            this.execTracker = null;
        }
    }

    @Test
    public void testBasicInvocation() {
        final LifecycleEnvironment le = mock(LifecycleEnvironment.class);

        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(le,
            poolName,
            false);

        this.execTracker = test.build();
        assertTrue(this.execTracker instanceof ScheduledThreadPoolExecutor);

        final ScheduledThreadPoolExecutor castedExec = (ScheduledThreadPoolExecutor) this.execTracker;
        assertFalse(castedExec.getRemoveOnCancelPolicy());

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(le, times(1)).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertSame(this.execTracker, esmCaptured.getExecutor());
        assertEquals(DEFAULT_SHUTDOWN_PERIOD, esmCaptured.getShutdownPeriod());
        assertSame(poolName, esmCaptured.getPoolName());
    }

    @Test
    public void testRemoveOnCancelTrue() {
        final LifecycleEnvironment le = mock(LifecycleEnvironment.class);

        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(le,
            poolName,
            false);

        this.execTracker = test.removeOnCancelPolicy(true).build();
        assertTrue(this.execTracker instanceof ScheduledThreadPoolExecutor);

        final ScheduledThreadPoolExecutor castedExec = (ScheduledThreadPoolExecutor) this.execTracker;
        assertTrue(castedExec.getRemoveOnCancelPolicy());

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(le, times(1)).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertSame(this.execTracker, esmCaptured.getExecutor());
        assertEquals(DEFAULT_SHUTDOWN_PERIOD, esmCaptured.getShutdownPeriod());
        assertSame(poolName, esmCaptured.getPoolName());
    }

    @Test
    public void testRemoveOnCancelFalse() {
        final LifecycleEnvironment le = mock(LifecycleEnvironment.class);

        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(le,
            poolName,
            false);

        this.execTracker = test.removeOnCancelPolicy(false).build();
        assertTrue(this.execTracker instanceof ScheduledThreadPoolExecutor);

        final ScheduledThreadPoolExecutor castedExec = (ScheduledThreadPoolExecutor) this.execTracker;
        assertFalse(castedExec.getRemoveOnCancelPolicy());

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(le, times(1)).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertSame(this.execTracker, esmCaptured.getExecutor());
        assertEquals(DEFAULT_SHUTDOWN_PERIOD, esmCaptured.getShutdownPeriod());
        assertSame(poolName, esmCaptured.getPoolName());
    }

    @Test
    public void testPredefinedThreadFactory() {
        final ThreadFactory tfactory = mock(ThreadFactory.class);
        final LifecycleEnvironment le = mock(LifecycleEnvironment.class);

        final String poolName = this.getClass().getSimpleName();

        final ScheduledExecutorServiceBuilder test = new ScheduledExecutorServiceBuilder(le,
            poolName,
            tfactory);

        this.execTracker = test.removeOnCancelPolicy(false).build();
        assertTrue(this.execTracker instanceof ScheduledThreadPoolExecutor);

        final ScheduledThreadPoolExecutor castedExec = (ScheduledThreadPoolExecutor) this.execTracker;
        assertFalse(castedExec.getRemoveOnCancelPolicy());
        assertSame(tfactory, castedExec.getThreadFactory());

        final ArgumentCaptor<ExecutorServiceManager> esmCaptor = ArgumentCaptor.forClass(ExecutorServiceManager.class);
        verify(le, times(1)).manage(esmCaptor.capture());

        final ExecutorServiceManager esmCaptured = esmCaptor.getValue();
        assertSame(this.execTracker, esmCaptured.getExecutor());
        assertEquals(DEFAULT_SHUTDOWN_PERIOD, esmCaptured.getShutdownPeriod());
        assertSame(poolName, esmCaptured.getPoolName());
    }
}
