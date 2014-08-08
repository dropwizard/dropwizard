package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/optional-return/")
@Produces(MediaType.TEXT_PLAIN)
public class OptionalReturnResource {
    @GET
    public Optional<String> showWithQueryParam(@QueryParam("id") String id) {
        return Optional.fromNullable(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Optional<String> showWithFormParam(@QueryParam("id") String id) {
        return Optional.fromNullable(id);
    }
}
