package io.dropwizard.auth;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.security.Principal;
import java.util.Optional;

@Path("/test/")
@Produces(MediaType.TEXT_PLAIN)
public class AuthResource {

    @RolesAllowed({"ADMIN"})
    @GET
    @Path("admin")
    public String show(@Auth Principal principal) {
        return "'" + principal.getName() + "' has admin privileges";
    }

    @PermitAll
    @GET
    @Path("profile")
    public String showForEveryUser(@Auth Principal principal) {
        return "'" + principal.getName() + "' has user privileges";
    }

    @PermitAll
    @GET
    @Path("optional")
    public String checkOptionalAuth(@Auth Optional<Principal> principalOpt) {
        return "principal was " + (principalOpt.isPresent() ? "" : "not ") + "present";
    }

    @GET
    @Path("implicit-permitall")
    public String implicitPermitAllAuthorization(@Auth Principal principal) {
        return "'" + principal.getName() + "' has user privileges";
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
