package io.dropwizard.jersey.sessions;

import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Objects;

@Path("/session/")
@Consumes(MediaType.TEXT_PLAIN)
@Produces(MediaType.TEXT_PLAIN)
public class SessionResource {

    @GET
    public String getName(@Session HttpSession session) {
        return Objects.toString(session.getAttribute("name"));
    }

    @POST
    public void setName(@Session HttpSession session,
                        String name) {
        session.setAttribute("name", name);
    }
}
