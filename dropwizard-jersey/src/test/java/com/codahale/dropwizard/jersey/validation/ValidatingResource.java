package com.codahale.dropwizard.jersey.validation;

import com.codahale.dropwizard.validation.Validated;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/valid/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ValidatingResource {
    @POST
    public String blah(@Validated ValidRepresentation representation) throws IOException {
        return representation.getName();
    }
}
