package io.dropwizard.jersey.filter;

import io.dropwizard.jersey.PATCH;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

@Path("/ping")
public class DummyResource {
    @GET
    public Response get() {
        return Response.ok().build();
    }

    @POST
    public Response post() {
        return Response.ok().build();
    }

    @PATCH
    public Response patch() {
        return Response.ok().build();
    }

    @PUT
    public Response put() {
        return Response.ok().build();
    }

    @DELETE
    public Response delete() {
        return Response.ok().build();
    }
}
