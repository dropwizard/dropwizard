package io.dropwizard.jersey.sessions;

import java.util.Objects;

import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
