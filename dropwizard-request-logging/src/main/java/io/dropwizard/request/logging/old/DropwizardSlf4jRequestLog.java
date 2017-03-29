package io.dropwizard.request.logging.old;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.RequestLog;

import java.io.IOException;
import java.util.TimeZone;

/**
 * A SLF4J-backed {@link RequestLog} implementation of {@link AbstractNCSARequestLog}.
 */
public class DropwizardSlf4jRequestLog extends AbstractNCSARequestLog {
    private final AppenderAttachableImpl<ILoggingEvent> appenders;

    /**
     * Creates a new request log.
     *
     * @param appenders the appenders to which requests will be logged
     * @param timeZone  the timezone to which timestamps will be converted
     */
    DropwizardSlf4jRequestLog(AppenderAttachableImpl<ILoggingEvent> appenders, TimeZone timeZone) {
        this.appenders = appenders;

        setLogLatency(true);
        setLogTimeZone(timeZone);
        setExtended(true);
        setPreferProxiedForAddress(true);

        // the appenders already started
        try {
            start();
        } catch (Exception e) {
            throw new IllegalStateException("Should have succeeded doing a noop start", e);
        }
    }

    @Override
    protected boolean isEnabled() {
        return true;
    }

    @Override
    public void write(String entry) throws IOException {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setLoggerName("http.request");
        event.setMessage(entry);
        event.setTimeStamp(System.currentTimeMillis());

        appenders.appendLoopOnAppenders(event);
    }

    void setLogTimeZone(TimeZone tz) {
        setLogTimeZone(tz.getID());
    }

    @Override
    protected void doStop() throws Exception {
        appenders.detachAndStopAllAppenders();
        super.doStop();
    }
}
