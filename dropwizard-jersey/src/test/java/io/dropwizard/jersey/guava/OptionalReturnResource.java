package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.core.MediaType;

@Path("/optional-return/")
@Produces(MediaType.TEXT_PLAIN)
public class OptionalReturnResource {
    @GET
    public Optional<String> showWithQueryParam(@QueryParam("id") String id) {
        return Optional.fromNullable(id);
    }

    @POST
    public Optional<String> showWithFormParam(@FormParam("id") String id) {
        return Optional.fromNullable(id);
    }
}
