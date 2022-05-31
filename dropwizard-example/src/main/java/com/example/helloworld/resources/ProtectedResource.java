package com.example.helloworld.resources;

import com.example.helloworld.core.User;
import io.dropwizard.auth.Auth;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/protected")
@Produces(MediaType.TEXT_PLAIN)
public class ProtectedResource {

    @PermitAll
    @GET
    public String showSecret(@Auth User user) {
        return String.format("Hey there, %s. You know the secret! %d", user.getName(), user.getId());
    }

    @RolesAllowed("ADMIN")
    @GET
    @Path("admin")
    public String showAdminSecret(@Auth User user) {
        return String.format("Hey there, %s. It looks like you are an admin. %d", user.getName(), user.getId());
    }
}
