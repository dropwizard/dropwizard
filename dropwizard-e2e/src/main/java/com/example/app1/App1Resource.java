package com.example.app1;

import io.dropwizard.views.common.View;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

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
