package io.dropwizard.metrics.jetty9.websockets;

import com.codahale.metrics.MetricRegistry;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.common.events.EventDriver;
import org.eclipse.jetty.websocket.common.events.EventDriverImpl;
import org.eclipse.jetty.websocket.jsr356.endpoints.EndpointInstance;
import org.eclipse.jetty.websocket.jsr356.endpoints.JsrEndpointEventDriver;
import org.eclipse.jetty.websocket.jsr356.server.PathParamServerEndpointConfig;

class InstJsrServerExtendsEndpointImpl implements EventDriverImpl {
    private final MetricRegistry metrics;

    public InstJsrServerExtendsEndpointImpl(MetricRegistry metrics) {
        this.metrics = metrics;
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
        return "class extends " + javax.websocket.Endpoint.class.getName();
    }

    @Override
    public boolean supports(Object websocket)
    {
        if (!(websocket instanceof EndpointInstance))
        {
            return false;
        }

        EndpointInstance ei = (EndpointInstance)websocket;
        Object endpoint = ei.getEndpoint();

        return (endpoint instanceof javax.websocket.Endpoint);
    }
}
