package com.example.helloworld.resources;

import com.example.helloworld.filter.DateRequired;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/filtered")
public class FilteredResource {

    @GET
    @DateRequired
    @Path("hello")
    public String sayHello() {
        return "hello";
    }
}
