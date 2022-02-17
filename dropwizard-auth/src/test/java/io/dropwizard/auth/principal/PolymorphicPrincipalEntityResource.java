package io.dropwizard.auth.principal;

import io.dropwizard.auth.Auth;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contains resource methods which are authenticated with
 * multi-principal injection.
 */
@Path("/auth-test")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.TEXT_PLAIN)
public class PolymorphicPrincipalEntityResource {
    @GET
    @Path("json-principal-entity")
    public String principalEntityWithoutAuth(@Auth JsonPrincipal principal) {
        assertThat(principal).isNotNull();
        return principal.getName();
    }

    @GET
    @Path("null-principal-entity")
    public String principalEntityWithoutAuth(@Auth NullPrincipal principal) {
        assertThat(principal).isNotNull();
        return principal.getName();
    }

    @GET
    @Path("optional")
    public String checkOptionalAuth(@Auth Optional<NullPrincipal> principalOpt) {
        return "principal was " + ((principalOpt.isPresent()) ? "" : "not ") + "present";
    }
}
