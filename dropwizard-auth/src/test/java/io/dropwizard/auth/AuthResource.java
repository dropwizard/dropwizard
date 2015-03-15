package io.dropwizard.auth;


import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

@Path("/test/")
@Produces(MediaType.TEXT_PLAIN)
public class AuthResource {
    @RolesAllowed({"ADMIN"})
    @GET
    public String show(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        return principal.getName();
    }

    @PermitAll
    @GET
    @Path("authnotrequired")
    public String showNotRequired(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        return principal == null ? "No Principal" : principal.getName();
    }

    @GET
    @Path("noauth")
    public String hello() {
        return "hello";
    }

    @DenyAll
    @GET
    @Path("denied")
    public String denied() {
        return "denied";
    }
}
