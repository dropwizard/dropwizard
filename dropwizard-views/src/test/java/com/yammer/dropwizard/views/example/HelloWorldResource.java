package com.yammer.dropwizard.views.example;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/hello")
@Produces(MediaType.TEXT_HTML)
public class HelloWorldResource {
    @GET
    public HelloWorldView show(@QueryParam("name") @DefaultValue("Stranger") String name) {
        return new HelloWorldView(new Person(name));
    }
}
