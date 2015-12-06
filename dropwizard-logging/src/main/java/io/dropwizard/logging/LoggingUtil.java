package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import io.dropwizard.util.Duration;
import org.slf4j.ILoggerFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.annotation.concurrent.GuardedBy;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LoggingUtil {
    private static final Duration LOGGER_CONTEXT_AWAITING_TIMEOUT = Duration.seconds(10);
    private static final Duration LOGGER_CONTEXT_AWAITING_SLEEP_TIME = Duration.milliseconds(100);

    @GuardedBy("julHijackingLock")
    private static boolean julHijacked = false;
    private static final Lock julHijackingLock = new ReentrantLock();

    private LoggingUtil() {
    }

    /**
     * Acquires the logger context.
     * <p/>
     * <p>It tries to correctly acquire the logger context in the multi-threaded environment.
     * Because of the <a href="bug">http://jira.qos.ch/browse/SLF4J-167</a> a thread, that didn't
     * start initialization has a possibility to get a reference not to a real context, but to a
     * substitute.</p>
     * <p>To work around this bug we spin-loop the thread with a sensible timeout, while the
     * context is not initialized. We can't just make this method synchronized, because
     * {@code  LoggerFactory.getILoggerFactory} doesn't safely publish own state. Threads can
     * observe a stale state, even if the logger has been already initialized. That's why this
     * method is not thread-safe, but it makes the best effort to return the correct result in
     * the multi-threaded environment.</p>
     */
    public static LoggerContext getLoggerContext() {
        final long startTime = System.nanoTime();
        while (true) {
            final ILoggerFactory iLoggerFactory = LoggerFactory.getILoggerFactory();
            if (iLoggerFactory instanceof LoggerContext) {
                return (LoggerContext) iLoggerFactory;
            }
            if ((System.nanoTime() - startTime) > LOGGER_CONTEXT_AWAITING_TIMEOUT.toNanoseconds()) {
                throw new IllegalStateException("Unable to acquire the logger context");
            }
            try {
                Thread.sleep(LOGGER_CONTEXT_AWAITING_SLEEP_TIME.toMilliseconds());
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Gets the root j.u.l.Logger and removes all registered handlers
     * then redirects all active j.u.l. to SLF4J
     * <p/>
     * N.B. This should only happen once, hence the flag and locking
     */
    public static void hijackJDKLogging() {
        julHijackingLock.lock();
        try {
            if (!julHijacked) {
                SLF4JBridgeHandler.removeHandlersForRootLogger();
                SLF4JBridgeHandler.install();
                julHijacked = true;
            }
        } finally {
            julHijackingLock.unlock();
        }
    }
}
