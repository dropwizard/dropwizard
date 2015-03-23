package io.dropwizard.auth;


import javax.servlet.http.HttpServletRequest;
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
    @Auth
    @GET
    public String show(@Context SecurityContext securityContext) {
        Principal principal = securityContext.getUserPrincipal();
        return principal.getName();
    }

    @Auth(required = false)
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
}
