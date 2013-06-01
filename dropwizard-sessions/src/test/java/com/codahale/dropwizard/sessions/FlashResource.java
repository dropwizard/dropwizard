package com.codahale.dropwizard.sessions;

import com.codahale.dropwizard.sessions.Flash;
import com.codahale.dropwizard.sessions.Session;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Objects;

@Path("/flash/")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class FlashResource {
    @Context UriInfo uriInfo;

    @POST
    public void setName(@Session Flash<String> flash,
                        String name) {
        flash.set(name);
    }

    @GET
    public String getName(@Session Flash<String> flash) {
        return Objects.toString(flash.get().orNull());
    }
}
