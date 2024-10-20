package io.dropwizard.lifecycle.setup;

import com.codahale.metrics.InstrumentedThreadFactory;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.slf4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ExecutorServiceBuilderTest {

    private static final String WARNING = "Parameter 'maximumPoolSize' is conflicting with unbounded work queues";

    private final MetricRegistry metricRegistry = new MetricRegistry();
    private ExecutorServiceBuilder executorServiceBuilder;
    private Logger log;

    @BeforeEach
    void setUp() {
        executorServiceBuilder = new ExecutorServiceBuilder(new LifecycleEnvironment(metricRegistry), "test-%d");
        log = mock(Logger.class);
        ExecutorServiceBuilder.setLog(log);
    }

    @Test
    void testGiveAWarningAboutMaximumPoolSizeAndUnboundedQueue() {
        executorServiceBuilder
            .minThreads(4)
            .maxThreads(8)
            .build();

        verify(log).warn(WARNING);
        assertThat(metricRegistry.getMetrics())
            .containsOnlyKeys("test.created", "test.terminated", "test.running");
    }

    @Test
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    void testGiveNoWarningAboutMaximumPoolSizeAndBoundedQueue() {
        ExecutorService exe = executorServiceBuilder
            .minThreads(4)
            .maxThreads(8)
            .workQueue(new ArrayBlockingQueue<>(16))
            .build();

        verify(log, never()).warn(WARNING);
        assertCanExecuteAtLeast2ConcurrentTasks(exe);
    }

    /**
     * There should be no warning about using an Executors.newSingleThreadExecutor() equivalent
     * @see java.util.concurrent.Executors#newSingleThreadExecutor()
     */
    @Test
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    void shouldNotWarnWhenSettingUpSingleThreadedPool() {
        executorServiceBuilder
            .minThreads(1)
            .maxThreads(1)
            .keepAliveTime(Duration.milliseconds(0))
            .workQueue(new LinkedBlockingQueue<>())
            .build();

        verify(log, never()).warn(anyString());
    }

    /**
     * There should be no warning about using an Executors.newCachedThreadPool() equivalent
     * @see java.util.concurrent.Executors#newCachedThreadPool()
     */
    @Test
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    void shouldNotWarnWhenSettingUpCachedThreadPool() {
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
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    void shouldNotWarnWhenUsingTheDefaultConfiguration() {
        executorServiceBuilder.build();
        verify(log, never()).warn(anyString());
    }

    /**
     * Setting large max threads without large min threads is misleading on the default queue implementation
     * It should warn or work
     */
    @Test
    @SuppressWarnings("Slf4jFormatShouldBeConst")
    void shouldBeAbleToExecute2TasksAtOnceWithLargeMaxThreadsOrBeWarnedOtherwise() {
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
    void shouldUseInstrumentedThreadFactory() {
        assertThat(executorServiceBuilder.build())
            .isInstanceOfSatisfying(ThreadPoolExecutor.class, castedExec ->
                assertThat(castedExec.getThreadFactory()).isInstanceOf(InstrumentedThreadFactory.class));
    }

    @CsvSource(value = {
        "my-client-%d,my-client",
        "my-client--%d,my-client-",
        "my-client-%d-abc,my-client-abc",
        "my-client%d,my-client",
        "my-client%d-abc,my-client-abc",
        "my-client%s,my-client",
        "my-client%sabc,my-clientabc",
        "my-client%10d,my-client",
        "my-client%10d0,my-client0",
        "my-client%-10d,my-client",
        "my-client%-10d0,my-client0",
        "my-client-%10d,my-client",
        "my-client-%10dabc,my-clientabc",
        "my-client-%1$d,my-client",
        "my-client-%1$d-abc,my-client-abc",
        "-%d,''",
        "%d,''",
        "%d-abc,-abc",
        "%10d,''",
        "%10dabc,abc",
        "%-10d,''",
        "%-10dabc,abc",
        "%10s,''" ,
        "%10sabc,abc",
    })
    @ParameterizedTest
    void nameWithoutFormat(String format, String name) {
        assertThat(ExecutorServiceBuilder.getNameWithoutFormat(format))
            .describedAs("%s -> %s", format, name)
            .isEqualTo(name);
    }

    /**
     * Tries to run 2 tasks that on the executor that rely on each others side effect to complete. If they fail to
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
