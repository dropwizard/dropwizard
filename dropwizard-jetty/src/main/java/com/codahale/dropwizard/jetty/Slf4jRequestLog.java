package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import org.eclipse.jetty.server.AbstractNCSARequestLog;
import org.eclipse.jetty.server.RequestLog;

import java.io.IOException;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * A SLF4J-backed {@link RequestLog} implementation of {@link AbstractNCSARequestLog}.
 */
public class Slf4jRequestLog extends AbstractNCSARequestLog {
    private final AppenderAttachableImpl<ILoggingEvent> appenders;

    /**
     * Creates a new request log.
     *
     * @param appenders     the appenders to which requests will be logged
     * @param timeZone      the timezone to which timestamps will be converted
     */
    public Slf4jRequestLog(AppenderAttachableImpl<ILoggingEvent> appenders, TimeZone timeZone) {
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
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        appenders.detachAndStopAllAppenders();
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

        appenders.appendLoopOnAppenders(event);
    }

    public void setLogTimeZone(TimeZone tz) {
        setLogTimeZone(tz.getID());
    }
}
