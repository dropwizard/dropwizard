package com.example.app1;

import java.util.OptionalInt;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import io.dropwizard.views.View;

@Path("/")
public class App1Resource {
    @GET
    @Path("empty-optional")
    public OptionalInt emptyOptional() {
        return OptionalInt.empty();
    }
    
    @GET
    @Path("view-with-missing-tpl")
    public View getMissingTemplateView() {
        return new View("not-found.mustache") {  
        };
    }
    
}
