package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.codahale.dropwizard.util.Duration;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.RequestLog;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A asynchronous {@link RequestLog} implementation of {@link AbstractNCSARequestLog}. Log entries
 * are added to an in-memory queue and an offline thread handles the responsibility of batching them
 * to the logging appenders.
 */
public class AsyncRequestLog extends AbstractNCSARequestLog {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();

    private class Dispatcher extends Thread {
        private final int batchSize;
        private final Duration batchDuration;
        private volatile boolean running = true;
        private final List<String> statements;

        private Dispatcher(int batchSize, Duration batchDuration) {
            super("async-request-log-dispatcher-" + THREAD_COUNTER.incrementAndGet());
            this.batchSize = batchSize;
            this.batchDuration = batchDuration;
            this.running = true;
            this.statements = Lists.newArrayListWithCapacity(batchSize);
            setDaemon(true);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    // drain until we have a full batch or the duration is up
                    Queues.drain(queue,
                                 statements,
                                 batchSize,
                                 batchDuration.getQuantity(),
                                 batchDuration.getUnit());
                    for (String statement : statements) {
                        final LoggingEvent event = new LoggingEvent();
                        event.setLevel(Level.INFO);
                        event.setMessage(statement);
                        appenders.appendLoopOnAppenders(event);
                    }

                    statements.clear();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void shutdown() {
            this.running = false;
        }
    }

    private final BlockingQueue<String> queue;
    private final Dispatcher dispatcher;
    private final AppenderAttachableImpl<ILoggingEvent> appenders;

    /**
     * Creates a new request log.
     *
     * @param appenders     the appenders to which requests will be logged
     * @param timeZone      the timezone to which timestamps will be converted
     * @param batchSize     the maximum number of requests to batch
     * @param batchDuration the maximum amount of time to wait for a full batch
     */
    public AsyncRequestLog(AppenderAttachableImpl<ILoggingEvent> appenders,
                           TimeZone timeZone,
                           int batchSize,
                           Duration batchDuration) {
        this.queue = new LinkedBlockingQueue<>();
        this.dispatcher = new Dispatcher(batchSize, batchDuration);
        this.appenders = appenders;

        setLogDispatch(true);
        setLogLatency(true);
        setLogTimeZone(timeZone);
        setExtended(true);
        setPreferProxiedForAddress(true);
    }

    @Override
    protected void doStart() throws Exception {
        final Iterator<Appender<ILoggingEvent>> iterator = appenders.iteratorForAppenders();
        while (iterator.hasNext()) {
            iterator.next().start();
        }
        dispatcher.start();
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        dispatcher.shutdown();
        appenders.detachAndStopAllAppenders();
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    @Override
    public void write(String requestEntry) throws IOException {
        queue.add(requestEntry);
    }

    public void setLogTimeZone(TimeZone tz) {
        setLogTimeZone(tz.getID());
    }
}
