package com.codahale.dropwizard.testing.junit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class TestResource {

    private final String message;

    public TestResource(String message) {
        this.message = message;
    }

    @Path("test")
    @GET
    public String test() {
        return message;
    }
}
