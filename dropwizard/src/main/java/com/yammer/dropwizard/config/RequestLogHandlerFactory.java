package com.yammer.dropwizard.config;

import com.yammer.dropwizard.jetty.AsyncRequestLog;
import org.eclipse.jetty.server.handler.RequestLogHandler;

import static com.yammer.dropwizard.config.HttpConfiguration.RequestLogConfiguration;

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
        final RequestLogHandler handler = new RequestLogHandler();
        handler.setRequestLog(new AsyncRequestLog(config.getFilenamePattern(),
                                                  config.getRetainedFileCount()));
        return handler;
    }
}
