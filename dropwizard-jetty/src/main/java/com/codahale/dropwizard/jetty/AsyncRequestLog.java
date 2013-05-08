package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.RequestLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A asynchronous {@link RequestLog} implementation of {@link AbstractNCSARequestLog}. Log entries
 * are added to an in-memory queue and an offline thread handles the responsibility of batching them
 * to disk.
 */
public class AsyncRequestLog extends AbstractNCSARequestLog {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final int BATCH_SIZE = 10000;

    private class Dispatcher extends Thread {
        private volatile boolean running = true;
        private final List<String> statements = new ArrayList<>(BATCH_SIZE);

        private Dispatcher() {
            super("async-request-log-dispatcher-" + THREAD_COUNTER.incrementAndGet());
            setDaemon(true);
        }

        @Override
        public void run() {
            while (running) {
                try {
                    statements.add(queue.take());
                    queue.drainTo(statements, BATCH_SIZE);

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

    public AsyncRequestLog(AppenderAttachableImpl<ILoggingEvent> appenders, TimeZone timeZone) {
        this.queue = new LinkedBlockingQueue<>();
        this.dispatcher = new Dispatcher();
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
