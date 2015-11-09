package io.dropwizard.metrics.jetty9.websockets.annotated;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer.Context;
import io.dropwizard.metrics.jetty9.websockets.EventDriverMetrics;
import javax.websocket.CloseReason;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.jsr356.annotations.JsrEvents;
import org.eclipse.jetty.websocket.jsr356.endpoints.EndpointInstance;
import org.eclipse.jetty.websocket.jsr356.endpoints.JsrAnnotatedEventDriver;

public class InstJsrAnnotatedEventDriver extends JsrAnnotatedEventDriver {
    private final EventDriverMetrics edm;

    public InstJsrAnnotatedEventDriver(WebSocketPolicy policy, EndpointInstance ei, JsrEvents<ServerEndpoint, ServerEndpointConfig> events, MetricRegistry metrics) {
        super(policy, ei, events);
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
        super.onConnect();
    }

    @Override
    public void onError(Throwable cause) {
        edm.exceptionMetered.ifPresent(Meter::mark);
        super.onError(cause);
    }

    @Override
    protected void onClose(CloseReason closereason) {
        edm.countOpened.ifPresent(Counter::dec);
        Context ctx = (Context) getJsrSession().getUserProperties().get(this.getClass().getName());
        if (ctx != null)
            ctx.close();
        super.onClose(closereason);
    }

}
