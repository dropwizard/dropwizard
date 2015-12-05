package com.example.helloworld.resources;

import io.dropwizard.views.View;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.nio.charset.StandardCharsets;

@Path("/views")
public class ViewResource {
    @GET
    @Produces("text/html;charset=UTF-8")
    @Path("/utf8.ftl")
    public View freemarkerUTF8() {
        return new View("/views/ftl/utf8.ftl", StandardCharsets.UTF_8) {
        };
    }

    @GET
    @Produces("text/html;charset=ISO-8859-1")
    @Path("/iso88591.ftl")
    public View freemarkerISO88591() {
        return new View("/views/ftl/iso88591.ftl", StandardCharsets.ISO_8859_1) {
        };
    }

    @GET
    @Produces("text/html;charset=UTF-8")
    @Path("/utf8.mustache")
    public View mustacheUTF8() {
        return new View("/views/mustache/utf8.mustache", StandardCharsets.UTF_8) {
        };
    }

    @GET
    @Produces("text/html;charset=ISO-8859-1")
    @Path("/iso88591.mustache")
    public View mustacheISO88591() {
        return new View("/views/mustache/iso88591.mustache", StandardCharsets.ISO_8859_1) {
        };
    }
}
