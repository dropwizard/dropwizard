package com.yammer.dropwizard.freemarker.example;

import com.sun.jersey.api.view.Viewable;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/hello")
@Produces(MediaType.TEXT_HTML)
public class TemplateResource {
    @GET
    public Viewable show(@QueryParam("name") @DefaultValue("Stranger") String name) {
        return new Viewable("hello.ftl", new Person(name));
    }
}
