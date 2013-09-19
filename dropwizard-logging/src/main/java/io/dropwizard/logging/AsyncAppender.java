package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import io.dropwizard.util.Duration;
import org.eclipse.jetty.util.ConcurrentArrayBlockingQueue;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An asynchronous appender. Log entries are added to an in-memory queue and an offline thread
 * handles the responsibility sending batches of events to the delegate. The worker thread will
 * wait for either a specific number of events or a specific amount of time to have passed before
 * processing events.
 */
public class AsyncAppender extends AppenderBase<ILoggingEvent> {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    private class Worker extends Thread {
        private final int batchSize;
        private final Duration batchDuration;
        private volatile boolean running = true;
        private final List<ILoggingEvent> events;

        private Worker(int batchSize, Duration batchDuration) {
            this.batchSize = batchSize;
            this.batchDuration = batchDuration;
            this.running = true;
            this.events = Lists.newArrayListWithCapacity(batchSize);
            setDaemon(true);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    // drain until we have a full batch or the duration is up
                    Queues.drain(queue,
                                 events,
                                 batchSize,
                                 batchDuration.getQuantity(),
                                 batchDuration.getUnit());
                    for (ILoggingEvent event : events) {
                        delegate.doAppend(event);
                    }

                    events.clear();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void shutdown() {
            this.running = false;
        }
    }

    private final BlockingQueue<ILoggingEvent> queue;
    private final Worker worker;
    private final Appender<ILoggingEvent> delegate;

    public AsyncAppender(Appender<ILoggingEvent> delegate,
                         int batchSize,
                         Duration batchDuration,
                         boolean bounded) {
        this.queue = buildQueue(batchSize, bounded);
        this.worker = new Worker(batchSize, batchDuration);
        this.delegate = delegate;
        setName("async-" + delegate.getName());
    }

    private ConcurrentArrayBlockingQueue<ILoggingEvent> buildQueue(int batchSize, boolean bounded) {
        if (bounded) {
            return new ConcurrentArrayBlockingQueue.Bounded<>(batchSize * 2);
        }
        return new ConcurrentArrayBlockingQueue.Unbounded<>();
    }

    @Override
    public void start() {
        super.start();
        worker.setName(getName() + "-" + THREAD_COUNTER.incrementAndGet());
        worker.start();
    }

    @Override
    public void stop() {
        super.stop();
        worker.shutdown();
    }

    @Override
    protected void append(ILoggingEvent event) {
        event.prepareForDeferredProcessing();
        try {
            queue.put(event);
        } catch (InterruptedException ignored) {
            // ruh-roh
        }
    }
}
