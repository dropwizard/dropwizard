package com.yammer.dropwizard.jetty;

// TODO: 10/12/11 <coda> -- write tests for AsyncRequestLog

import org.eclipse.jetty.http.HttpHeaders;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.DateCache;
import org.eclipse.jetty.util.RolloverFileOutputStream;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A non-blocking, asynchronous {@link RequestLog} implementation which implements a subset of the
 * functionality of {@link org.eclipse.jetty.server.NCSARequestLog}. Log entries are added to an
 * in-memory queue and an offline thread handles the responsibility of batching them to disk. The
 * date format is fixed, UTC time zone is fixed, and latency is always logged.
 */
public class AsyncRequestLog extends AbstractLifeCycle implements RequestLog {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger();
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncRequestLog.class);
    private static final int BATCH_SIZE = 10000;

    private class Dispatcher implements Runnable {
        private volatile boolean running = true;
        private final List<String> statements = new ArrayList<String>(BATCH_SIZE);

        @Override
        public void run() {
            while (running) {
                try {
                    statements.add(queue.take());
                    queue.drainTo(statements, BATCH_SIZE);
                    for (String statement : statements) {
                        writer.println(statement);
                    }
                    writer.flush();
                    statements.clear();
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void stop() {
            this.running = true;
        }
    }

    private static final ThreadLocal<DateCache> DATE_CACHE = new ThreadLocal<DateCache>() {
        @Override
        protected DateCache initialValue() {
            final DateCache cache = new DateCache("dd/MMM/yyyy:HH:mm:ss Z",
                                                  Locale.getDefault());
            cache.setTimeZoneID("UTC");
            return cache;
        }
    };

    private final BlockingQueue<String> queue;
    private final String filenamePattern;
    private final int numberOfFilesToRetain;
    private final Thread dispatchThread;
    private final Dispatcher dispatcher;
    private PrintWriter writer;

    /**
     * Creates a new {@link AsyncRequestLog}.
     *
     * @param filenamePattern          The filename pattern to which the log statements will be
     *                                 written. If {@code filenamePattern} contains the string
     *                                 {@code yyyy_mm_dd}, the file will be rotated every day, with
     *                                 {@code yyyy_mm_dd} being replaced by the year, month, and
     *                                 day. If {@code filenamePattern} is {@code null}, statements
     *                                 will be logged to STDOUT.
     * @param numberOfFilesToRetain    If {@code filenamePattern} is to be rotated, the number of
     *                                 total log files (including the active one) to be kept.
     */
    public AsyncRequestLog(String filenamePattern, int numberOfFilesToRetain) {
        this.filenamePattern = filenamePattern;
        this.numberOfFilesToRetain = numberOfFilesToRetain;
        this.writer = null;
        this.queue = new LinkedBlockingQueue<String>();
        this.dispatcher = new Dispatcher();
        this.dispatchThread = new Thread(dispatcher);
        dispatchThread.setName("async-request-log-dispatcher-" + THREAD_COUNTER.incrementAndGet());
        dispatchThread.setDaemon(true);
    }

    @Override
    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "UseOfSystemOutOrSystemErr"})
    protected void doStart() throws Exception {
        if (filenamePattern == null) {
            this.writer = new PrintWriter(System.out);
        } else {
            final RolloverFileOutputStream outputStream = new RolloverFileOutputStream(
                    filenamePattern,
                    true,
                    numberOfFilesToRetain,
                    TimeZone.getTimeZone("UTC"));
            this.writer = new PrintWriter(outputStream);
            LOGGER.info("Opened {}", outputStream.getDatedFilename());
        }

        dispatchThread.start();
    }

    @Override
    protected void doStop() throws Exception {
        dispatcher.stop();
        if (writer != null) {
            writer.close();
        }
    }

    @Override
    public void log(Request request, Response response) {
        // copied almost entirely from NCSARequestLog
        final StringBuilder buf = new StringBuilder(256);
        String address = request.getHeader(HttpHeaders.X_FORWARDED_FOR);
        if (address == null) {
            address = request.getRemoteAddr();
        }

        buf.append(address);
        buf.append(" - ");
        final Authentication authentication = request.getAuthentication();
        if (authentication instanceof Authentication.User) {
            buf.append(((Authentication.User) authentication).getUserIdentity()
                                                             .getUserPrincipal()
                                                             .getName());
        } else {
            buf.append(" - ");
        }

        buf.append(" [");
        buf.append(DATE_CACHE.get().format(request.getTimeStamp()));

        buf.append("] \"");
        buf.append(request.getMethod());
        buf.append(' ');
        buf.append(request.getUri().toString());
        buf.append(' ');
        buf.append(request.getProtocol());
        buf.append("\" ");
        if (request.getAsyncContinuation().isInitial()) {
            int status = response.getStatus();
            if (status <= 0) {
                status = 404;
            }
            buf.append((char) ('0' + ((status / 100) % 10)));
            buf.append((char) ('0' + ((status / 10) % 10)));
            buf.append((char) ('0' + (status % 10)));
        } else {
            buf.append("Async");
        }

        final long responseLength = response.getContentCount();
        if (responseLength >= 0) {
            buf.append(' ');
            if (responseLength > 99999) {
                buf.append(responseLength);
            } else {
                if (responseLength > 9999) {
                    buf.append((char) ('0' + ((responseLength / 10000) % 10)));
                }
                if (responseLength > 999) {
                    buf.append((char) ('0' + ((responseLength / 1000) % 10)));
                }
                if (responseLength > 99) {
                    buf.append((char) ('0' + ((responseLength / 100) % 10)));
                }
                if (responseLength > 9) {
                    buf.append((char) ('0' + ((responseLength / 10) % 10)));
                }
                buf.append((char) ('0' + (responseLength % 10)));
            }
            buf.append(' ');
        } else {
            buf.append(" - ");
        }

        final long now = System.currentTimeMillis();
        final long dispatchTime = request.getDispatchTime();

        buf.append(' ');
        buf.append(now - ((dispatchTime == 0) ? request.getTimeStamp() : dispatchTime));

        buf.append(' ');
        buf.append(now - request.getTimeStamp());

        queue.add(buf.toString());
    }
}
