package io.dropwizard.jersey.dummy;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import org.glassfish.jersey.server.ManagedAsync;

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
