package com.codahale.dropwizard.jersey.dummy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class DummyResource {
    @GET
    public String foo() {
        return "bar";
    }
}
