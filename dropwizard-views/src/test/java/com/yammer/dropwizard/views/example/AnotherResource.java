package com.yammer.dropwizard.views.example;

import com.yammer.dropwizard.views.MyOtherView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/yay")
@Produces(MediaType.TEXT_HTML)
public class AnotherResource {
    @GET
    public MyOtherView performYay() {
        return new MyOtherView();
    }
}
