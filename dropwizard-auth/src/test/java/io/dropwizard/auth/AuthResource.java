package io.dropwizard.auth;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
