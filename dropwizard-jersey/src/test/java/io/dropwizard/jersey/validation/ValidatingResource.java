package io.dropwizard.jersey.validation;

import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;

@Path("/valid/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ValidatingResource {
    @POST
    @Path("foo")
    public String blah(@Validated ValidRepresentation representation) throws IOException {
        return representation.getName();
    }

    @GET
    @Path("bar")
    @Length(max = 3)
    public String blaze(@QueryParam("name") @Length(min = 3) String name) {
        return name;
    }

    @GET
    @Path("zoo")
    public String blazer(@Valid @BeanParam BeanParameter params) {
        return params.getName();
    }
}
