package com.example.validation;

import io.dropwizard.validation.OneOf;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ValidatedResource {

    @GET
    @Path("/injectable")
    public void injectableValidation(@QueryParam("value") @OneOf("right") @WasInjected String value) {
        //Do nothing
    }

    @GET
    @Path("/default")
    public void defaultValidation(@QueryParam("value") @OneOf("right") String value) {
        //Do nothing
    }
}
