package com.example.helloworld.resources;

import com.example.helloworld.core.User;
import io.dropwizard.auth.Auth;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;

/**
 * {@link RolesAllowed}, {@link PermitAll} are supported on the class level.<p>
 * Method level annotations take precedence over the class level ones
 */

@Path("/protected")
@RolesAllowed("BASIC_GUY")
public final class ProtectedClassResource {

    @GET
    @PermitAll
    @Path("guest")
    public String showSecret(@Auth User user) {
        return String.format("Hey there, %s. You know the secret! %d", user.getName(), user.getId());
    }

    /* Access to this method is authorized by the class level annotation */
    @GET
    public String showBasicUserSecret(@Context SecurityContext context) {
        User user = (User) context.getUserPrincipal();
        return String.format("Hey there, %s. You seem to be a basic user. %d", user.getName(), user.getId());
    }

    @GET
    @RolesAllowed("ADMIN")
    @Path("admin")
    public String showAdminSecret(@Auth User user) {
        return String.format("Hey there, %s. It looks like you are an admin. %d", user.getName(), user.getId());
    }

}
