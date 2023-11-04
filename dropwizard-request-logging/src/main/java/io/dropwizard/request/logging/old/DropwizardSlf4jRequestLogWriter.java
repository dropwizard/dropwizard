package io.dropwizard.request.logging.old;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A SLF4J-backed {@link RequestLog.Writer}.
 *
 * @since 2.0
 */
public class DropwizardSlf4jRequestLogWriter extends AbstractLifeCycle implements RequestLog.Writer {
    private AppenderAttachableImpl<ILoggingEvent> appenders;
    private final LoggerContext loggerContext;

    DropwizardSlf4jRequestLogWriter(AppenderAttachableImpl<ILoggingEvent> appenders) {
        this.appenders = appenders;
        this.loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    @Override
    public void write(String entry) throws IOException {
        final LoggingEvent event = new LoggingEvent();
        event.setLevel(Level.INFO);
        event.setLoggerName("http.request");
        event.setMessage(entry);
        event.setTimeStamp(System.currentTimeMillis());
        event.setLoggerContext(loggerContext);

        appenders.appendLoopOnAppenders(event);
    }

    @Override
    protected void doStop() throws Exception {
        appenders.detachAndStopAllAppenders();
        super.doStop();
    }
}
