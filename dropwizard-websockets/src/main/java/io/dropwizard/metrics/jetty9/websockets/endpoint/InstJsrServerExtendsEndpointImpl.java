package io.dropwizard.metrics.jetty9.websockets.endpoint;

import com.codahale.metrics.MetricRegistry;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.common.events.EventDriver;
import org.eclipse.jetty.websocket.common.events.EventDriverImpl;
import org.eclipse.jetty.websocket.jsr356.endpoints.EndpointInstance;
import org.eclipse.jetty.websocket.jsr356.endpoints.JsrEndpointEventDriver;
import org.eclipse.jetty.websocket.jsr356.server.JsrServerExtendsEndpointImpl;
import org.eclipse.jetty.websocket.jsr356.server.PathParamServerEndpointConfig;

public class InstJsrServerExtendsEndpointImpl implements EventDriverImpl {
    private final MetricRegistry metrics;
    private final JsrServerExtendsEndpointImpl origImpl;

    public InstJsrServerExtendsEndpointImpl(MetricRegistry metrics) {
        this.metrics = metrics;
        this.origImpl = new JsrServerExtendsEndpointImpl();
    }

    @Override
    public EventDriver create(Object websocket, WebSocketPolicy policy)
    {
        if (!(websocket instanceof EndpointInstance))
        {
            throw new IllegalStateException(String.format("Websocket %s must be an %s",websocket.getClass().getName(),EndpointInstance.class.getName()));
        }
        
        EndpointInstance ei = (EndpointInstance)websocket;
        JsrEndpointEventDriver driver = new InstJsrEndpointEventDriver(policy, ei, metrics);
        
        ServerEndpointConfig config = (ServerEndpointConfig)ei.getConfig();
        if (config instanceof PathParamServerEndpointConfig)
        {
            PathParamServerEndpointConfig ppconfig = (PathParamServerEndpointConfig)config;
            driver.setPathParameters(ppconfig.getPathParamMap());
        }

        return driver;
    }

    @Override
    public String describeRule()
    {
        return origImpl.describeRule();
    }

    @Override
    public boolean supports(Object websocket)
    {
        return origImpl.supports(websocket);
    }
}
