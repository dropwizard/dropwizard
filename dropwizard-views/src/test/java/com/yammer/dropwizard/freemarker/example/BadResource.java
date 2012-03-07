package com.yammer.dropwizard.freemarker.example;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/bad")
public class BadResource {
    @GET
    public BadView messUp() {
        return new BadView();
    }
}
