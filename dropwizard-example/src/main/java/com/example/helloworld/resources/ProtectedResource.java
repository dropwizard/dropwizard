package com.example.helloworld.resources;

import com.example.helloworld.core.User;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Path("/protected")
@Produces(MediaType.TEXT_PLAIN)
public class ProtectedResource {
    @RolesAllowed("ADMIN")
    @GET
    public String showSecret(@Context SecurityContext context) {
        User userPrincipal = (User) context.getUserPrincipal();
        return String.format("Hey there, %s. You know the secret! %d", userPrincipal.getName(), userPrincipal.getId());
    }
}
