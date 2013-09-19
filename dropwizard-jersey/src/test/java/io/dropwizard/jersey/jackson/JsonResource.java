package io.dropwizard.jersey.jackson;

import com.google.common.collect.ImmutableList;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;

@Path("/json/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JsonResource {
    @POST
    @Path("/broken")
    public void broken(BrokenRepresentation rep) throws IOException {
        System.out.println(rep);
    }

    @POST
    @Path("/ok")
    public List<String> ok(OkRepresentation rep) throws IOException {
        return ImmutableList.of(rep.getMessage());
    }
}
