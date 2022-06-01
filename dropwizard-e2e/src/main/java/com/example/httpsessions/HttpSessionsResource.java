package com.example.httpsessions;

import io.dropwizard.jersey.sessions.Session;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class HttpSessionsResource {
    @GET
    @Path("session")
    public Response isSessionInjected(@Session HttpSession httpSession) {
        return Response.ok(httpSession != null).build();
    }
}
