package com.example.app1;

import io.dropwizard.views.View;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.OptionalInt;

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
    public Map<String, String> postMapper(Map<String, String> map) {
        final Map<String, String> resultMap = new LinkedHashMap<>(map);
        resultMap.put("hello", "world");
        return resultMap;
    }

    @GET
    @Path("custom-class")
    public CustomClass newCustomClass() {
        return new CustomClass();
    }
}
