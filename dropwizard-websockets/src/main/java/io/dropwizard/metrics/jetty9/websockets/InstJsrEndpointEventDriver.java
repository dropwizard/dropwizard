package io.dropwizard.metrics.jetty9.websockets;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.websocket.CloseReason;
import org.eclipse.jetty.websocket.api.WebSocketPolicy;
import org.eclipse.jetty.websocket.jsr356.endpoints.EndpointInstance;
import org.eclipse.jetty.websocket.jsr356.endpoints.JsrEndpointEventDriver;

public class InstJsrEndpointEventDriver extends JsrEndpointEventDriver {
    private final EventDriverMetrics edm;

    public InstJsrEndpointEventDriver(WebSocketPolicy policy, EndpointInstance ei, MetricRegistry metrics) {
        super(policy, ei);
        this.edm = new EventDriverMetrics(metadata.getEndpointClass(), metrics);
    }

    @Override
    public void onTextFrame(ByteBuffer buffer, boolean fin) throws IOException {
        super.onTextFrame(buffer, fin); 
        if (activeMessage==null) // finished message
            edm.onTextMeter.ifPresent(Meter::mark);
    }

    @Override
    public void onError(Throwable cause) {
        edm.exceptionMetered.ifPresent(Meter::mark);
        super.onError(cause); 
    }

    @Override
    public void onConnect() {
        edm.countOpened.ifPresent(Counter::inc);
        edm.timer.ifPresent(e -> getJsrSession().getUserProperties().put(this.getClass().getName(), e.time()));
        super.onConnect(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onClose(CloseReason closereason) {
        edm.countOpened.ifPresent(Counter::dec);
        super.onClose(closereason); //To change body of generated methods, choose Tools | Templates.
        Timer.Context ctx = (Timer.Context) getJsrSession().getUserProperties().get(this.getClass().getName());
        if (ctx != null)
            ctx.close();
    }
}
