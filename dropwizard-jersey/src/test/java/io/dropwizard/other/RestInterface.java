package io.dropwizard.other;

import io.dropwizard.jersey.validation.ValidRepresentation;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

/**
 * Interface that holds all the javax.ws.rs annotations so we keep our
 * implementation a little cleaner. This interface lives in a different
 * package as the tests are set up to scan everything in io.dropwizard.jersey
 * which will pick up this class. Jersey will then fail to instantiate this
 * class. Users that typically register their classes directly will Jersey
 * will not need to worry about this problem.
 */
@Path("/valid2/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RestInterface {
    @POST
    @Path("repr")
    @Valid
    ValidRepresentation repr(@NotNull @Valid ValidRepresentation representation,
                             @NotNull @QueryParam("interfaceVariable") String xer);
}
