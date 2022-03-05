package com.example.httpsessions;

import io.dropwizard.jersey.sessions.Session;

import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/")
public class HttpSessionsResource {
    @GET
    @Path("session")
    public Response isSessionInjected(@Session HttpSession httpSession) {
        return Response.ok(httpSession != null).build();
    }
}
