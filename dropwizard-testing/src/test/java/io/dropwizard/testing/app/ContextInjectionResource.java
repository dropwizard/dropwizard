package io.dropwizard.testing.app;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.PATCH;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;

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

    @PATCH
    public String echoPatch(String patchMessage) {
        return patchMessage;
    }

}
