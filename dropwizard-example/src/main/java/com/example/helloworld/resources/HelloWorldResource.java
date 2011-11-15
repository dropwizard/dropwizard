package com.example.helloworld.resources;

import com.example.helloworld.core.Saying;
import com.example.helloworld.core.Template;
import com.google.common.base.Optional;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.core.TimerMetric;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;

@Path("/hello-world")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource {
    private static final TimerMetric GETS = Metrics.newTimer(HelloWorldResource.class,
                                                             "get-requests");

    private final Template template;
    private final AtomicLong counter;

    public HelloWorldResource(Template template) {
        this.template = template;
        this.counter = new AtomicLong();
    }

    @GET
    public Saying sayHello(@QueryParam("name") String name) {
        final TimerContext context = GETS.time();
        try {
            return new Saying(counter.incrementAndGet(), template.render(Optional.fromNullable(name)));
        } finally {
            context.stop();
        }
    }
}
