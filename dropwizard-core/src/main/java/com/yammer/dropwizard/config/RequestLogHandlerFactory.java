package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import com.yammer.dropwizard.jetty.AsyncRequestLog;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.slf4j.LoggerFactory;

// TODO: 11/7/11 <coda> -- document RequestLogHandlerFactory
// TODO: 11/7/11 <coda> -- test RequestLogHandlerFactory

public class RequestLogHandlerFactory {
    private final RequestLogConfiguration config;

    public RequestLogHandlerFactory(RequestLogConfiguration config) {
        this.config = config;
    }
    
    public boolean isEnabled() {
        return config.isEnabled();
    }

    public RequestLogHandler build() {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);

        final FileAppender<ILoggingEvent> appender = buildAppender(logger,
                                                                   config.getCurrentLogFilename(),
                                                                   config.getArchivedLogFilenamePattern(),
                                                                   config.getArchivedFileCount());


        final RequestLogHandler handler = new RequestLogHandler();
        handler.setRequestLog(new AsyncRequestLog(appender, config.getTimeZone()));

        return handler;
    }

    private FileAppender<ILoggingEvent> buildAppender(Logger logger,
                                                      String currentLogFilename,
                                                      String archivedLogFilenamePattern,
                                                      int archivedFileCount) {
        final DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> triggeringPolicy =
                new DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent>();
        triggeringPolicy.setContext(logger.getLoggerContext());

        final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<ILoggingEvent>();
        rollingPolicy.setContext(logger.getLoggerContext());
        rollingPolicy.setFileNamePattern(archivedLogFilenamePattern);
        rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(
                triggeringPolicy);
        triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
        rollingPolicy.setMaxHistory(archivedFileCount);

        final EchoLayout<ILoggingEvent> layout = new EchoLayout<ILoggingEvent>();
        layout.start();

        final RollingFileAppender<ILoggingEvent> a = new RollingFileAppender<ILoggingEvent>();
        a.setAppend(true);
        a.setContext(logger.getLoggerContext());
        a.setLayout(layout);
        a.setFile(currentLogFilename);
        a.setPrudent(false);
        a.setRollingPolicy(rollingPolicy);
        a.setTriggeringPolicy(triggeringPolicy);

        rollingPolicy.setParent(a);
        rollingPolicy.start();

        a.stop();
        a.start();

        return a;
    }
}
