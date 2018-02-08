package io.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import io.dropwizard.logging.layout.DiscoverableLayoutFactory;

import javax.annotation.concurrent.GuardedBy;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility class to configure logging before the dropwizard yml
 * configuration has been read, parsed, and the provided logging
 * strategy has been applied.
 * <p/>
 * N.B. The methods in this class have run once semantics,
 * multiple calls are idempotent
 */
public class BootstrapLogging {

    @GuardedBy("BOOTSTRAPPING_LOCK")
    private static boolean bootstrapped = false;
    private static final Lock BOOTSTRAPPING_LOCK = new ReentrantLock();

    private BootstrapLogging() {
    }

    // initially configure for WARN+ console logging
    public static void bootstrap() {
        bootstrap(Level.WARN);
    }

    public static void bootstrap(Level level) {
        bootstrap(level, DropwizardLayout::new);
    }

    public static void bootstrap(Level level, DiscoverableLayoutFactory<ILoggingEvent> layoutFactory) {
        LoggingUtil.hijackJDKLogging();

        BOOTSTRAPPING_LOCK.lock();
        try {
            if (bootstrapped) {
                return;
            }
            final Logger root = LoggingUtil.getLoggerContext().getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
            root.detachAndStopAllAppenders();

            final Layout<ILoggingEvent> layout = layoutFactory.build(root.getLoggerContext(), TimeZone.getDefault());
            layout.start();

            final ThresholdFilter filter = new ThresholdFilter();
            filter.setLevel(level.toString());
            filter.start();

            final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
            appender.addFilter(filter);
            appender.setContext(root.getLoggerContext());

            final LayoutWrappingEncoder<ILoggingEvent> layoutEncoder = new LayoutWrappingEncoder<>();
            layoutEncoder.setLayout(layout);
            appender.setEncoder(layoutEncoder);
            appender.start();

            root.addAppender(appender);
            bootstrapped = true;
        } finally {
            BOOTSTRAPPING_LOCK.unlock();
        }
    }
}
