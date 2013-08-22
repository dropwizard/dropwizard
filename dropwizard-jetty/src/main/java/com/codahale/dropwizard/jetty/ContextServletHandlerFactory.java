package com.codahale.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.hibernate.validator.constraints.NotEmpty;

/** TODO: Document */
public class ContextServletHandlerFactory extends AbstractServletHandlerFactory {

    @NotEmpty
    private String contextPath = "/";

    public ContextServletHandlerFactory(String contextPath) {
        this.contextPath = contextPath;
    }

    @JsonProperty("path")
    public String getContextPath() {
        return contextPath;
    }

    @JsonProperty("path")
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public Handler build(Server server,
                         MutableServletContextHandler contextHandler,
                         MetricRegistry metricRegistry,
                         String name) {
        final Handler handler = super.build(server, contextHandler, metricRegistry, name);
        contextHandler.setContextPath(contextPath);
        return handler;
    }
}
