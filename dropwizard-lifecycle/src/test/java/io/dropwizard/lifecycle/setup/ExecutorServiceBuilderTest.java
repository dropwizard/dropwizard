package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedThreadFactory;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.slf4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadPoolExecutor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ExecutorServiceBuilderTest {

    private static final String WARNING = "Parameter 'maximumPoolSize' is conflicting with unbounded work queues";

    private ExecutorServiceBuilder executorServiceBuilder;
    private Logger log;

    @BeforeEach
    public void setUp() throws Exception {
        executorServiceBuilder = new ExecutorServiceBuilder(new LifecycleEnvironment(new MetricRegistry()), "test");
        log = mock(Logger.class);
        ExecutorServiceBuilder.setLog(log);
    }

    @Test
    public void testGiveAWarningAboutMaximumPoolSizeAndUnboundedQueue() {
        executorServiceBuilder
            .minThreads(4)
            .maxThreads(8)
            .build();

        verify(log).warn(WARNING);
    }

    @Test
    public void testGiveNoWarningAboutMaximumPoolSizeAndBoundedQueue() throws InterruptedException {
        ExecutorService exe = executorServiceBuilder
            .minThreads(4)
            .maxThreads(8)
            .workQueue(new ArrayBlockingQueue<>(16))
            .build();

        verify(log, never()).warn(WARNING);
        assertCanExecuteAtLeast2ConcurrentTasks(exe);
    }

    /**
     * There should be no warning about using a Executors.newSingleThreadExecutor() equivalent
     * @see java.util.concurrent.Executors#newSingleThreadExecutor()
     */
    @Test
    public void shouldNotWarnWhenSettingUpSingleThreadedPool() {
        executorServiceBuilder
            .minThreads(1)
            .maxThreads(1)
            .keepAliveTime(Duration.milliseconds(0))
            .workQueue(new LinkedBlockingQueue<>())
            .build();

        verify(log, never()).warn(anyString());
    }

    /**
     * There should be no warning about using a Executors.newCachedThreadPool() equivalent
     * @see java.util.concurrent.Executors#newCachedThreadPool()
     */
    @Test
    public void shouldNotWarnWhenSettingUpCachedThreadPool() throws InterruptedException {
        ExecutorService exe = executorServiceBuilder
            .minThreads(0)
            .maxThreads(Integer.MAX_VALUE)
            .keepAliveTime(Duration.seconds(60))
            .workQueue(new SynchronousQueue<>())
            .build();

        verify(log, never()).warn(anyString());
        assertCanExecuteAtLeast2ConcurrentTasks(exe); // cached thread pools work right?
    }

    @Test
    public void shouldNotWarnWhenUsingTheDefaultConfiguration() {
        executorServiceBuilder.build();
        verify(log, never()).warn(anyString());
    }

    /**
     * Setting large max threads without large min threads is misleading on the default queue implementation
     * It should warn or work
     */
    @Test
    public void shouldBeAbleToExecute2TasksAtOnceWithLargeMaxThreadsOrBeWarnedOtherwise() {
        ExecutorService exe = executorServiceBuilder
            .maxThreads(Integer.MAX_VALUE)
            .build();

        try {
            verify(log).warn(anyString());
        } catch (WantedButNotInvoked error) {
            // no warning has been given so we should be able to execute at least 2 things at once
            assertCanExecuteAtLeast2ConcurrentTasks(exe);
        }
    }

    @Test
    public void shouldUseInstrumentedThreadFactory() {
        ExecutorService exe = executorServiceBuilder.build();
        final ThreadPoolExecutor castedExec = (ThreadPoolExecutor) exe;

        assertThat(castedExec.getThreadFactory()).isInstanceOf(InstrumentedThreadFactory.class);
    }

    /**
     * Tries to run 2 tasks that on the executor that rely on each others side-effect to complete. If they fail to
     * complete within a short time then we can assume they are not running concurrently
     * @param exe an executor to try to run 2 tasks on
     */
    private void assertCanExecuteAtLeast2ConcurrentTasks(Executor exe) {
        CountDownLatch latch = new CountDownLatch(2);
        Runnable concurrentLatchCountDownAndWait = () -> {
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        };

        exe.execute(concurrentLatchCountDownAndWait);
        exe.execute(concurrentLatchCountDownAndWait);

        try {
            // 1 second is ages even on a slow VM
            assertThat(latch.await(1, TimeUnit.SECONDS))
                .as("2 tasks executed concurrently on " + exe)
                .isTrue();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}
