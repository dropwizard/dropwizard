package io.dropwizard.testing.app;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.testing.Person;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
}
