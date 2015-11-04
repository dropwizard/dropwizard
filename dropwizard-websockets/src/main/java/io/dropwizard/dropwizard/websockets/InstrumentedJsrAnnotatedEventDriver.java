package io.dropwizard.dropwizard.websockets;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import com.codahale.metrics.annotation.Metered;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.websocket.CloseReason;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.jsr356.annotations.JsrEvents;
import org.eclipse.jetty.websocket.jsr356.endpoints.EndpointInstance;
import org.eclipse.jetty.websocket.jsr356.endpoints.JsrAnnotatedEventDriver;
import org.eclipse.jetty.websocket.jsr356.server.AnnotatedServerEndpointMetadata;

public class InstrumentedJsrAnnotatedEventDriver extends JsrAnnotatedEventDriver {
    private final AnnotatedServerEndpointMetadata annmd;
    private MetricRegistry metrics;
    private Optional<Meter> onTextMeter = Optional.empty();

    private InstrumentedJsrAnnotatedEventDriver(WebSocketPolicy policy, EndpointInstance endpointInstance, JsrEvents<?, ?> events) {
        super(policy, endpointInstance, events);
        annmd = (AnnotatedServerEndpointMetadata) endpointInstance.getMetadata();
    }

    public InstrumentedJsrAnnotatedEventDriver(WebSocketPolicy policy, EndpointInstance ei, JsrEvents<ServerEndpoint, ServerEndpointConfig> events, MetricRegistry metrics) {
        this(policy, ei, events);
        this.metrics = metrics;
        Method onText = annmd.onText.getMethod();
        final Metered annotation = onText.getAnnotation(Metered.class);
        if (annotation != null) {
            this.onTextMeter = Optional.of(metrics.meter(name(annotation.name(), onText.getDeclaringClass().getName(), onText.getName())));
        }
    }

    @Override
    public void onTextMessage(String message) {
        onTextMeter.ifPresent(Meter::mark);
        super.onTextMessage(message); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onConnect() {
        System.out.println("XXXXX " + annmd.onOpen.getMethod().getName());
        super.onConnect(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onError(Throwable cause) {
        super.onError(cause); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onClose(CloseReason closereason) {
        System.out.println("XXXXX " + annmd.onClose.getMethod().getName());
        super.onClose(closereason); //To change body of generated methods, choose Tools | Templates.
    }

}
