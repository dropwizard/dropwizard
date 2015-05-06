package io.dropwizard.jersey.validation;

import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/valid/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ValidatingResource {
    @POST
    @Path("foo")
    public String blah(@Validated ValidRepresentation representation) {
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

    @GET
    @Path("head")
    public String heads(@HeaderParam("cheese") @NotEmpty String secretSauce) {
        return secretSauce;
    }

    @GET
    @Path("cooks")
    public String cooks(@CookieParam("user_id") @NotEmpty String secretSauce) {
        return secretSauce;
    }

    @GET
    @Path("goods/{id}")
    public String pather(@PathParam("id") @Email String is) {
        return is;
    }

    @POST
    @Path("form")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String form(@FormParam("username") @NotEmpty String secretSauce) {
        return secretSauce;
    }

    @GET
    @Path("nested")
    @Valid
    public WrappedValidRepresentation nested() {
        WrappedValidRepresentation result = new WrappedValidRepresentation();
        result.setRepresentation(new ValidRepresentation());
        return result;
    }

    @GET
    @Path("nested2")
    @Valid
    public WrappedFailingExample nested2() {
        WrappedFailingExample result = new WrappedFailingExample();
        result.setExample(new FailingExample());
        return result;
    }

    @GET
    @Path("context")
    public String contextual(@Valid @Context @NotNull ServletContext con) {
        return "A";
    }

    @GET
    @Path("matrix")
    public String matrixParam(@MatrixParam("bob") @NotEmpty String param) {
        return param;
    }

    @POST
    @Path("nothing")
    public FailingExample valmeth(@Valid FailingExample exam) {
        return exam;
    }
}
