package io.dropwizard.cdi;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("dummy")
public class DummyResource {

    @Inject
    @Named("dummy")
    private String dummy;

    @GET
    public Response someResourceMethod() {
        return Response.ok().entity(dummy).build();
    }
}
