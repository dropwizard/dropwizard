package com.example.app1;

import java.util.OptionalInt;

import com.google.common.collect.ImmutableMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import io.dropwizard.views.View;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class App1Resource {
    @GET
    @Path("empty-optional")
    public OptionalInt emptyOptional() {
        return OptionalInt.empty();
    }

    @GET
    @Path("view-with-missing-tpl")
    @Produces(MediaType.TEXT_HTML)
    public View getMissingTemplateView() {
        return new View("not-found.mustache") {
        };
    }

    @POST
    @Path("mapper")
    public ImmutableMap<String, String> postMapper(ImmutableMap<String, String> map) {
        return ImmutableMap.<String, String>builder().putAll(map).put("hello", "world").build();
    }

    @GET
    @Path("custom-class")
    public CustomClass newCustomClass() {
        return new CustomClass();
    }
}
