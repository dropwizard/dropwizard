package io.dropwizard.documentation.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class MyResource {
    @GET
    public String hello() {
        return "Hello, world!";
    }
}
