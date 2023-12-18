package io.dropwizard.request.logging.logback.access.handler;

import io.dropwizard.core.server.ServerCustomizer;
import io.dropwizard.jetty.MutableServletContextHandler;
import org.eclipse.jetty.server.Handler;

public class LogbackAccessServerCustomizer implements ServerCustomizer {

    @Override
    public Handler customizeHandlerChain(Handler first) {
        if (!(first instanceof Handler.Singleton singleton)) {
            throw new IllegalStateException("Expecting Handler to be an instance of Handler.Singleton");
        }

        MutableServletContextHandler mutableServletContextHandler = singleton.getDescendant(MutableServletContextHandler.class);
        if (mutableServletContextHandler == null) {
            throw new IllegalStateException("Expecting MutableServletContextHandler to be present in the Jetty handler chain");
        }
        mutableServletContextHandler.insertHandler(new LogbackAccessRequestLogAwareHandler());

        return first;
    }
}
