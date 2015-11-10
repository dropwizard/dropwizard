package io.dropwizard.metrics.jetty9.websockets;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.ExceptionMetered;
import com.codahale.metrics.annotation.Metered;
import com.codahale.metrics.annotation.Timed;
import java.util.Optional;
import javax.websocket.OnError;
import javax.websocket.OnMessage;

public class EventDriverMetrics {
    public final Optional<Meter> onTextMeter;
    public final Optional<Counter> countOpened;
    public final Optional<Timer> timer;
    public final Optional<Meter> exceptionMetered;

    public EventDriverMetrics(final Class<?> endpointClass, MetricRegistry metrics) {
        final Class<?> klass = endpointClass;
        Metered metered = klass.getAnnotation(Metered.class);
        Timed timed = klass.getAnnotation(Timed.class);
        ExceptionMetered em = klass.getAnnotation(ExceptionMetered.class);
        this.onTextMeter = metered != null
                ? Optional.of(metrics.meter(MetricRegistry.name(metered.name(), klass.getName(), OnMessage.class.getSimpleName())))
                : Optional.empty();
        this.countOpened = metered != null
                ? Optional.of(metrics.counter(MetricRegistry.name(metered.name(), klass.getName(), OPEN_CONNECTIONS)))
                : Optional.empty();
        this.timer = timed != null
                ? Optional.of(metrics.timer(MetricRegistry.name(timed.name(), klass.getName())))
                : Optional.empty();
        this.exceptionMetered = em != null
                ? Optional.of(metrics.meter(MetricRegistry.name(em.name(), klass.getName(), OnError.class.getSimpleName())))
                : Optional.empty();
    }
    public static final String OPEN_CONNECTIONS = "openConnections";

}
