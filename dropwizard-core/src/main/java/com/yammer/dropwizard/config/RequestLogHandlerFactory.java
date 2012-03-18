package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.yammer.dropwizard.jetty.AsyncRequestLog;
import com.yammer.dropwizard.logging.LogbackFactory;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.slf4j.LoggerFactory;

import static com.yammer.dropwizard.config.LoggingConfiguration.ConsoleConfiguration;
import static com.yammer.dropwizard.config.LoggingConfiguration.FileConfiguration;

// TODO: 11/7/11 <coda> -- document RequestLogHandlerFactory
// TODO: 11/7/11 <coda> -- test RequestLogHandlerFactory

public class RequestLogHandlerFactory {
    private final RequestLogConfiguration config;

    public RequestLogHandlerFactory(RequestLogConfiguration config) {
        this.config = config;
    }
    
    public boolean isEnabled() {
        return config.getConsoleConfiguration().isEnabled() ||
                config.getFileConfiguration().isEnabled() ||
                config.getSyslogConfiguration().isEnabled();
    }

    public RequestLogHandler build() {
        final Logger logger = (Logger) LoggerFactory.getLogger("http.request");
        logger.setAdditive(false);
        final LoggerContext context = logger.getLoggerContext();

        final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<ILoggingEvent>();

        final EchoLayout<ILoggingEvent> layout = new EchoLayout<ILoggingEvent>();
        layout.start();

        final ConsoleConfiguration console = config.getConsoleConfiguration();
        if (console.isEnabled()) {
            final ConsoleAppender<ILoggingEvent> appender = LogbackFactory.buildConsoleAppender(console,
                                                                                                context);
            appender.stop();
            appender.setLayout(layout);
            appender.start();
            appenders.addAppender(appender);
        }

        final FileConfiguration file = config.getFileConfiguration();
        if (file.isEnabled()) {
            final RollingFileAppender<ILoggingEvent> appender = LogbackFactory.buildFileAppender(file,
                                                                                                 context);

            appender.stop();
            appender.setLayout(layout);
            appender.start();
            appenders.addAppender(appender);
        }

        final LoggingConfiguration.SyslogConfiguration syslog = config.getSyslogConfiguration();
        if (syslog.isEnabled()) {
            final SyslogAppender appender = LogbackFactory.buildSyslogAppender(syslog, context);

            appender.stop();
            appender.setLayout(layout);
            appender.start();
            appenders.addAppender(appender);
        }

        final RequestLogHandler handler = new RequestLogHandler();
        handler.setRequestLog(new AsyncRequestLog(appenders, config.getTimeZone()));

        return handler;
    }
}
