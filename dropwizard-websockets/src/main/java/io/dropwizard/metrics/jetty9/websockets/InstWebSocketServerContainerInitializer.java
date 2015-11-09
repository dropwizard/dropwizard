package io.dropwizard.metrics.jetty9.websockets;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.metrics.jetty9.websockets.annotated.InstJsrServerEndpointImpl;
import io.dropwizard.metrics.jetty9.websockets.endpoint.InstJsrServerExtendsEndpointImpl;
import javax.servlet.ServletException;
import org.eclipse.jetty.websocket.common.events.EventDriverFactory;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;

public class InstWebSocketServerContainerInitializer {
    public static ServerContainer configureContext(final MutableServletContextHandler context, final MetricRegistry metrics) throws ServletException {
        WebSocketUpgradeFilter filter = WebSocketUpgradeFilter.configureContext(context);
        ServerContainer wsContainer = new ServerContainer(filter, filter.getFactory(), context.getServer().getThreadPool());
        EventDriverFactory edf = filter.getFactory().getEventDriverFactory();
        edf.clearImplementations();

        edf.addImplementation(new InstJsrServerEndpointImpl(metrics));
        edf.addImplementation(new InstJsrServerExtendsEndpointImpl(metrics));
        context.addBean(wsContainer);
        context.setAttribute(javax.websocket.server.ServerContainer.class.getName(), wsContainer);
        return wsContainer;
    }
}
