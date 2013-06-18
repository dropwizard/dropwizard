package com.codahale.dropwizard.jersey.guava;

import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/optional-param/")
@Produces(MediaType.TEXT_PLAIN)
public class OptionalParamResource {
    @GET
    public String show(@QueryParam("id") Optional<Integer> id) {
        return id.or(-1).toString();
    }
}
