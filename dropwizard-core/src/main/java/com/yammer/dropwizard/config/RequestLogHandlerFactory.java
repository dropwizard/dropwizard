package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.yammer.dropwizard.jetty.AsyncRequestLog;
import com.yammer.dropwizard.logging.LoggingOutput;
import com.yammer.metrics.core.Clock;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.slf4j.LoggerFactory;

// TODO: 11/7/11 <coda> -- document RequestLogHandlerFactory
// TODO: 11/7/11 <coda> -- test RequestLogHandlerFactory

public class RequestLogHandlerFactory {
    private static class RequestLogLayout extends LayoutBase<ILoggingEvent> {
        @Override
        public String doLayout(ILoggingEvent event) {
            return event.getFormattedMessage() + CoreConstants.LINE_SEPARATOR;
        }
    }

    private final RequestLogConfiguration config;
    private final String name;

    public RequestLogHandlerFactory(RequestLogConfiguration config, String name) {
        this.config = config;
        this.name = name;
    }
    
    public boolean isEnabled() {
        return !config.getOutputs().isEmpty();
    }

    public RequestLogHandler build() {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);
        final LoggerContext context = logger.getLoggerContext();

        final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<ILoggingEvent>();

        final RequestLogLayout layout = new RequestLogLayout();
        layout.start();

        for (LoggingOutput output : config.getOutputs()) {
            appenders.addAppender(output.build(context, name, layout));
        }

        final RequestLogHandler handler = new RequestLogHandler();
        handler.setRequestLog(new AsyncRequestLog(Clock.defaultClock(),
                                                  appenders,
                                                  config.getTimeZone()));

        return handler;
    }
}
