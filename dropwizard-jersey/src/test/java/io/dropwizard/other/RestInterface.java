package io.dropwizard.other;

import io.dropwizard.jersey.validation.ValidRepresentation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

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
