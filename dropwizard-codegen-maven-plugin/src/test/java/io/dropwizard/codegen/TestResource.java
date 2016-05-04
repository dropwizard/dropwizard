package io.dropwizard.codegen;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/something")
@Produces(MediaType.APPLICATION_JSON)
public class TestResource {

    @GET
    @Path("/get")
    public Something getSomething() {
        return new Something();
    }

    @POST
    @Path("/add")
    public Something addSomething(Something something) {
        return something;
    }
}
