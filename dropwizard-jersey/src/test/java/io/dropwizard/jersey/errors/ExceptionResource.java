package io.dropwizard.jersey.errors;

import com.fasterxml.jackson.databind.JsonMappingException;

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

    @GET
    @Path("json-mapping-exception")
    public void jsonMappingException() throws JsonMappingException {
        throw new JsonMappingException("BOOM");
    }
}
