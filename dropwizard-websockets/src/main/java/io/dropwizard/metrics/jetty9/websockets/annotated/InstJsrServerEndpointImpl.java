package io.dropwizard.metrics.jetty9.websockets.annotated;

import com.codahale.metrics.MetricRegistry;
import static io.dropwizard.websockets.GeneralUtils.rethrow;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.common.events.EventDriver;
import org.eclipse.jetty.websocket.common.events.EventDriverImpl;
import org.eclipse.jetty.websocket.jsr356.annotations.JsrEvents;
import org.eclipse.jetty.websocket.jsr356.annotations.OnMessageCallable;
import org.eclipse.jetty.websocket.jsr356.endpoints.EndpointInstance;
import org.eclipse.jetty.websocket.jsr356.endpoints.JsrAnnotatedEventDriver;
import org.eclipse.jetty.websocket.jsr356.server.AnnotatedServerEndpointMetadata;
import org.eclipse.jetty.websocket.jsr356.server.JsrServerEndpointImpl;
import org.eclipse.jetty.websocket.jsr356.server.PathParamServerEndpointConfig;

public class InstJsrServerEndpointImpl implements EventDriverImpl {
    private final MetricRegistry metrics;
    private final JsrServerEndpointImpl origImpl;
    private final Method getMaxMessageSizeMethod;

    public InstJsrServerEndpointImpl(MetricRegistry metrics) {
        super();
        this.metrics = metrics;
        this.origImpl = new JsrServerEndpointImpl();
        this.getMaxMessageSizeMethod = rethrow(() -> this.origImpl.getClass().getDeclaredMethod("getMaxMessageSize",int.class,OnMessageCallable[].class)).get();
        getMaxMessageSizeMethod.setAccessible(true);
    }

    @Override
    public EventDriver create(Object websocket, WebSocketPolicy policy) throws Throwable {
        if (!(websocket instanceof EndpointInstance)) {
            throw new IllegalStateException(String.format("Websocket %s must be an %s", websocket.getClass().getName(), EndpointInstance.class.getName()));
        }

        EndpointInstance ei = (EndpointInstance) websocket;
        AnnotatedServerEndpointMetadata metadata = (AnnotatedServerEndpointMetadata) ei.getMetadata();
        JsrEvents<ServerEndpoint, ServerEndpointConfig> events = new JsrEvents<>(metadata);

        // Handle @OnMessage maxMessageSizes
        int maxBinaryMessage = getMaxMessageSize(policy.getMaxBinaryMessageSize(), metadata.onBinary, metadata.onBinaryStream);
        int maxTextMessage = getMaxMessageSize(policy.getMaxTextMessageSize(), metadata.onText, metadata.onTextStream);

        policy.setMaxBinaryMessageSize(maxBinaryMessage);
        policy.setMaxTextMessageSize(maxTextMessage);

        //////// instrumentation is here
        JsrAnnotatedEventDriver driver = new InstJsrAnnotatedEventDriver(policy, ei, events, metrics);
        ////////
        
        // Handle @PathParam values
        ServerEndpointConfig config = (ServerEndpointConfig) ei.getConfig();
        if (config instanceof PathParamServerEndpointConfig) {
            PathParamServerEndpointConfig ppconfig = (PathParamServerEndpointConfig) config;
            driver.setPathParameters(ppconfig.getPathParamMap());
        }

        return driver;
    }

    @Override
    public String describeRule() {
        return origImpl.describeRule();
    }

    private int getMaxMessageSize(int defaultMaxMessageSize, OnMessageCallable... onMessages) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        return (int) getMaxMessageSizeMethod.invoke(origImpl, defaultMaxMessageSize, onMessages);        
    }

    @Override
    public boolean supports(Object websocket) {
        return origImpl.supports(websocket);
    }
}
