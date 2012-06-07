package com.yammer.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadFactory;

public class AsyncAppender extends AppenderBase<ILoggingEvent> implements Runnable {
    private static final int BATCH_SIZE = 1000;

    public static Appender<ILoggingEvent> wrap(Appender<ILoggingEvent> delegate) {
        final AsyncAppender appender = new AsyncAppender(delegate, BATCH_SIZE);
        appender.start();
        return appender;
    }

    private static final ThreadFactory THREAD_FACTORY =
            new ThreadFactoryBuilder().setNameFormat("async-log-appender-%d")
                                      .setDaemon(true)
                                      .build();

    private final Appender<ILoggingEvent> delegate;
    private final BlockingQueue<ILoggingEvent> queue;
    private final List<ILoggingEvent> batch;
    private final Thread dispatcher;
    private final int batchSize;
    private volatile boolean running;

    public AsyncAppender(Appender<ILoggingEvent> delegate, int batchSize) {
        this.delegate = delegate;
        this.queue = Queues.newLinkedBlockingQueue();
        this.batch = Lists.newArrayListWithCapacity(batchSize);
        this.batchSize = batchSize;
        this.dispatcher = THREAD_FACTORY.newThread(this);
        setContext(delegate.getContext());
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        queue.add(eventObject);
    }

    @Override
    public void start() {
        super.start();
        this.running = true;
        dispatcher.start();
    }

    @Override
    public void stop() {
        this.running = false;
        try {
            dispatcher.join();
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
        super.stop();
    }

    @Override
    public void run() {
        while (running) {
            try {
                batch.add(queue.take());
                queue.drainTo(batch, batchSize - 1);

                for (ILoggingEvent event : batch) {
                    delegate.doAppend(event);
                }

                batch.clear();
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
