package io.dropwizard.auth.principal;

import io.dropwizard.auth.Auth;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
}
