package io.dropwizard.jersey.jackson;

import com.google.common.collect.ImmutableList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.net.URL;
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
        return ImmutableList.of(rep.getMessage());
    }

    @POST
    @Path("/brokenList")
    public List<Integer> ok(List<BrokenRepresentation> rep) {
        return ImmutableList.of(rep.size());
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
