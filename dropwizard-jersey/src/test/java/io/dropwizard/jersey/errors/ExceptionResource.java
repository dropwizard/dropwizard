package io.dropwizard.jersey.errors;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

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

    @GET
    @Path("html-exception")
    @Produces(MediaType.TEXT_HTML)
    public void htmlException() throws WebApplicationException {
        throw new WebApplicationException("BIFF", Response.Status.BAD_REQUEST);
    }
}
