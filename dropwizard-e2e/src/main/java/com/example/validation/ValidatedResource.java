package com.example.validation;

import io.dropwizard.validation.OneOf;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;


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

    @POST
    @Path("/bean-validation")
    public void beanValidation(@Valid @NotNull ValidatedBean bean) {
        if (bean == null
            || bean.getString() == null || bean.getString().trim().isEmpty()
            || bean.getNumber() < 0
            || bean.getList().isEmpty()) {
            throw new IllegalArgumentException();
        }
    }
}
