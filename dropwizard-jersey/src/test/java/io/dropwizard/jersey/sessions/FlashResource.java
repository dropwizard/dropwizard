package io.dropwizard.jersey.sessions;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Objects;

@Path("/flash/")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class FlashResource {

    @POST
    public void setName(@Session Flash<String> flash,
                        String name) {
        flash.set(name);
    }

    @GET
    public String getName(@Session Flash<String> flash) {
        return Objects.toString(flash.get().orElse(null));
    }
}
