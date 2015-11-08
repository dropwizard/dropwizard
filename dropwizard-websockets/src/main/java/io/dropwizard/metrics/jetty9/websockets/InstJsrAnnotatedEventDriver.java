package io.dropwizard.metrics.jetty9.websockets;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import javax.websocket.CloseReason;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.jsr356.annotations.JsrEvents;
import org.eclipse.jetty.websocket.jsr356.endpoints.EndpointInstance;
import org.eclipse.jetty.websocket.jsr356.endpoints.JsrAnnotatedEventDriver;

public class InstJsrAnnotatedEventDriver extends JsrAnnotatedEventDriver {
    private EventDriverMetrics edm;

    private InstJsrAnnotatedEventDriver(WebSocketPolicy policy, EndpointInstance endpointInstance, JsrEvents<?, ?> events) {
        super(policy, endpointInstance, events);
    }

    public InstJsrAnnotatedEventDriver(WebSocketPolicy policy, EndpointInstance ei, JsrEvents<ServerEndpoint, ServerEndpointConfig> events, MetricRegistry metrics) {
        this(policy, ei, events);
        this.edm = new EventDriverMetrics(metadata.getEndpointClass(), metrics);
    }


    @Override
    public void onTextMessage(String message) {
        edm.onTextMeter.ifPresent(Meter::mark);
        super.onTextMessage(message);
    }

    @Override
    public void onConnect() {
        edm.countOpened.ifPresent(Counter::inc);
        edm.timer.ifPresent(e -> getJsrSession().getUserProperties().put(this.getClass().getName(), e.time()));
        super.onConnect(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onError(Throwable cause) {
        edm.exceptionMetered.ifPresent(Meter::mark);
        super.onError(cause); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onClose(CloseReason closereason) {
        edm.countOpened.ifPresent(Counter::dec);
        super.onClose(closereason); //To change body of generated methods, choose Tools | Templates.
        Context ctx = (Context) getJsrSession().getUserProperties().get(this.getClass().getName());
        if (ctx != null)
            ctx.close();

    }

}
