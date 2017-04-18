package io.dropwizard.codegen;

import javax.ws.rs.*;
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

    @PUT
    @Path("/update")
    public Something updateSomething(Something something) {
        return something;
    }

    @DELETE
    @Path("/delete")
    public void deleteSomething(int id) {

    }

}
