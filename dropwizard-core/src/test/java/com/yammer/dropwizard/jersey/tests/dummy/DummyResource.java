package com.yammer.dropwizard.jersey.tests.dummy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class DummyResource {
    @GET
    public String foo() {
        return "bar";
    }
}
