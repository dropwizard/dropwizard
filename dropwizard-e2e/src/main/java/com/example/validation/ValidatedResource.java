package com.example.validation;

import io.dropwizard.validation.OneOf;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
        // Do nothing
    }

    @GET
    @Path("/default")
    public void defaultValidation(@QueryParam("value") @OneOf("right") String value) {
        // Do nothing
    }

    @POST
    @Path("/bean-validation")
    public void beanValidation(@Valid @NotNull ValidatedBean bean) {
        if (bean == null
                || bean.getString() == null
                || bean.getString().trim().isEmpty()
                || bean.getNumber() < 0
                || bean.getList().isEmpty()) {
            throw new IllegalArgumentException();
        }
    }
}
