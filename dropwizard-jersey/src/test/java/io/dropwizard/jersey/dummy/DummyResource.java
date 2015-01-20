package io.dropwizard.jersey.dummy;

import org.glassfish.jersey.server.ManagedAsync;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;

@Path("/")
public class DummyResource {
    @GET
    public String foo() {
        return "bar";
    }

    @GET
    @Path("/async")
    @ManagedAsync
    public void async(@Suspended final AsyncResponse ar) {
        ar.resume("foobar");
    }
}
