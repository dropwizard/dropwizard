package io.dropwizard.jersey.sessions;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
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
