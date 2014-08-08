package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/optional-param/")
@Produces(MediaType.TEXT_PLAIN)
public class OptionalParamResource {
    @GET
    public String showWithQueryParam(@QueryParam("id") Optional<Integer> id) {
        return id.or(-1).toString();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String showWithFormParam(@FormParam("id") Optional<Integer> id) {
        return id.or(-1).toString();
    }
}
