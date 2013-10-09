package io.dropwizard.jersey.errors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/exception/")
@Produces(MediaType.APPLICATION_JSON)
public class ExceptionResource {
    @GET
    public String show() throws IOException {
        throw new IOException("WHAT");
    }
}
