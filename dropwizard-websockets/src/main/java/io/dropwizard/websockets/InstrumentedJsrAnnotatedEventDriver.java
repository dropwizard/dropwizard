package io.dropwizard.websockets;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import static com.codahale.metrics.MetricRegistry.name;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
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
    private Optional<Meter> onTextMeter = Optional.empty();
    private Optional<Counter> countOpened;
    private Optional<Timer> timer;
    private Optional<Meter> exceptionMetered;

    private InstrumentedJsrAnnotatedEventDriver(WebSocketPolicy policy, EndpointInstance endpointInstance, JsrEvents<?, ?> events) {
        super(policy, endpointInstance, events);
        annmd = (AnnotatedServerEndpointMetadata) endpointInstance.getMetadata();
    }

    public InstrumentedJsrAnnotatedEventDriver(WebSocketPolicy policy, EndpointInstance ei, JsrEvents<ServerEndpoint, ServerEndpointConfig> events, MetricRegistry metrics) {
        this(policy, ei, events);
        Metered metered = annmd.getEndpointClass().getAnnotation(Metered.class);
        Timed timed = annmd.getEndpointClass().getAnnotation(Timed.class);
        ExceptionMetered em = annmd.getEndpointClass().getAnnotation(ExceptionMetered.class);
        if (metered != null) {
            this.onTextMeter = Optional.of(metrics.meter(name(metered.name(), annmd.getEndpointClass().getName(), annmd.onText.getMethod().getName())));
            this.countOpened = Optional.of(metrics.counter(name(metered.name(), annmd.getEndpointClass().getName(),"openConnections")));
        }
        if (timed != null) 
            this.timer = Optional.of(metrics.timer(name(timed.name(), annmd.getEndpointClass().getName())));
        
        if (em != null)
            this.exceptionMetered = Optional.of(metrics.meter(name(em.name(),annmd.getEndpointClass().getName(),annmd.onError.getMethod().getName())));
    }

    @Override
    public void onTextMessage(String message) {
        onTextMeter.ifPresent(Meter::mark);
        super.onTextMessage(message);
    }

    @Override
    public void onConnect() {
        countOpened.ifPresent(Counter::inc);
        timer.ifPresent(e -> getJsrSession().getUserProperties().put(this.getClass().getName(), e.time()));
        super.onConnect(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onError(Throwable cause) {
        exceptionMetered.ifPresent(Meter::mark);
        super.onError(cause); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void onClose(CloseReason closereason) {
        countOpened.ifPresent(Counter::dec);
        super.onClose(closereason); //To change body of generated methods, choose Tools | Templates.
        Context ctx = (Context) getJsrSession().getUserProperties().get(this.getClass().getName());
        if (ctx != null)
            ctx.close();

    }

}
