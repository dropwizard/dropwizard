package com.codahale.dropwizard.sessions;

import com.codahale.dropwizard.sessions.Session;

import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
