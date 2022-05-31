package io.dropwizard.jersey.filter;

import io.dropwizard.jersey.PATCH;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

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
