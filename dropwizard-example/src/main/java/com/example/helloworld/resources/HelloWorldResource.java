package com.example.helloworld.resources;

import com.example.helloworld.core.Saying;
import com.example.helloworld.core.Template;
import com.google.common.base.Optional;
import com.wordnik.swagger.core.Api;
import com.wordnik.swagger.core.ApiOperation;
import com.yammer.dropwizard.jersey.caching.CacheControl;
import com.yammer.dropwizard.logging.Log;
import com.yammer.dropwizard.swagger.SwaggerResource;
import com.yammer.metrics.annotation.Timed;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Path("/hello-world.json")
@Api(value = "/hello-world", description = "Hello World Resource")
@Produces(MediaType.APPLICATION_JSON)
public class HelloWorldResource extends SwaggerResource {
    private static final Log LOG = Log.forClass(HelloWorldResource.class);

    private final Template template;
    private final AtomicLong counter;

    public HelloWorldResource(Template template) {
        this.template = template;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed(name = "get-requests")
    @CacheControl(maxAge = 1, maxAgeUnit = TimeUnit.DAYS)
    @ApiOperation(value = "Say Hello", responseClass = "com.example.helloworld.core.Saying")
    public Saying sayHello(@QueryParam("name") Optional<String> name) {
        return new Saying(counter.incrementAndGet(), template.render(name));
    }

    @POST
    @ApiOperation("Receive a hello message")
    public void receiveHello(@Valid Saying saying) {
        LOG.info("Received a saying: {}", saying);
    }
}
