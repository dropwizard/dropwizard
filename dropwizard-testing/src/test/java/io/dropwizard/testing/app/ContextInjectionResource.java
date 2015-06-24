package io.dropwizard.testing.app;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/test")
@Produces(MediaType.APPLICATION_JSON)
public class ContextInjectionResource {
    @GET
    @Timed
    public String getUriPath(@Context UriInfo uriInfo) {
        return uriInfo.getPath();
    }

    @POST
    public String getThis() {
        throw new RuntimeException("Can't touch this");
    }
}
