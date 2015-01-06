package io.dropwizard.lifecycle.setup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class ExecutorServiceBuilderTest {

    private static final String WARNING = "Parameter 'maximumPoolSize' is conflicting with unbounded work queues";

    private ExecutorServiceBuilder executorServiceBuilder;
    private Logger log;

    @Before
    public void setUp() throws Exception {
        executorServiceBuilder = new ExecutorServiceBuilder(new LifecycleEnvironment(), "test");
        executorServiceBuilder.minThreads(4);
        executorServiceBuilder.maxThreads(8);

        log = mock(Logger.class);
        ExecutorServiceBuilder.setLog(log);
    }

    @Test
    public void testGiveAWarningAboutMaximumPoolSizeAndUnboundedQueue() {
        executorServiceBuilder.build();

        verify(log).warn(WARNING);
    }

    @Test
    public void testGiveNoWarningAboutMaximumPoolSizeAndBoundedQueue() {
        executorServiceBuilder.workQueue(new ArrayBlockingQueue<Runnable>(16));

        executorServiceBuilder.build();

        verify(log, never()).warn(WARNING);
    }
}
