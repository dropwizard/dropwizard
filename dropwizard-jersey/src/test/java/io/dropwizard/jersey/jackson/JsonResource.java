package io.dropwizard.jersey.jackson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@Path("/json/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JsonResource {
    @POST
    @Path("/broken")
    public void broken(BrokenRepresentation rep) {
        System.out.println(rep);
    }

    @GET
    @Path("/brokenOutbound")
    public NonBeanImplementation brokenOutbound() {
        return new NonBeanImplementation();
    }

    @POST
    @Path("/ok")
    public List<Integer> ok(OkRepresentation rep) {
        return Collections.singletonList(rep.getMessage());
    }

    @POST
    @Path("/brokenList")
    public List<Integer> ok(List<BrokenRepresentation> rep) {
        return Collections.singletonList(rep.size());
    }

    @POST
    @Path("/custom")
    public void custom(CustomRepresentation rep) {
    }

    @POST
    @Path("/url")
    public void url(URL url) {
    }

    @POST
    @Path("/urlList")
    public void urlList(List<URL> url) {
    }

    @POST
    @Path("/interface")
    public void face(IInterface inter) {
    }

    @POST
    @Path("/interfaceList")
    public void face(List<IInterface> inter) {
    }

    private interface IInterface {
    }
}
