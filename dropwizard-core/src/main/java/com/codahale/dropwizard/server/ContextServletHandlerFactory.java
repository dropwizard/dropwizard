package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * An implementation of {@link ServletHandlerFactory} that is dependent context path.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>path</td>
 *         <td>/</td>
 *         <td>The context path of the servlets for this handler.</td>
 *     </tr>
 * </table>
 * For more configuration parameters, see {@link AbstractServletHandlerFactory}.
 *
 * @see ServletHandlerFactory
 * @see AbstractServletHandlerFactory
 */
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
