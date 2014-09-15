package io.dropwizard.auth;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/test/")
@Produces(MediaType.TEXT_PLAIN)
public class AuthResource {
    @GET
    public String show(@Auth String principal) {
        return principal;
    }
}
