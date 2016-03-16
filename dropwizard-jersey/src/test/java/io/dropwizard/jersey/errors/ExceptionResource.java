package io.dropwizard.jersey.errors;

import com.fasterxml.jackson.databind.JsonMappingException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

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
        throw new JsonMappingException(new StringReader(""), "BOOM");
    }

    @GET
    @Path("web-application-exception")
    public void webApplicationException() throws WebApplicationException {
        throw new WebApplicationException("KAPOW", Response.Status.BAD_REQUEST);
    }

    @GET
    @Path("web-application-exception-with-redirect")
    public void webApplicationExceptionWithRedirect() throws WebApplicationException {
        URI redirectPath = UriBuilder.fromPath("/exception/redirect-target").build();
        throw new WebApplicationException(Response.seeOther(redirectPath).build());
    }

    @GET
    @Path("redirect-target")
    public Response redirectTarget() {
        return Response.ok().entity("{\"status\":\"OK\"}").build();
    }
}
