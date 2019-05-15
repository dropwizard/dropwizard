package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.Example;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.ListExample;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.Partial1;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.Partial2;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.PartialExample;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.jersey.params.LongParam;
import io.dropwizard.jersey.params.NonEmptyStringParam;
import io.dropwizard.validation.Validated;
import org.hibernate.validator.constraints.Length;

import javax.servlet.ServletContext;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.valueextraction.Unwrapping;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.CookieParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;

@Path("/valid/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ValidatingResource {

    @QueryParam("sort")
    @Pattern(regexp = "^(asc|desc)$")
    private String sortParam = "";

    @POST
    @Path("foo")
    @Valid
    public ValidRepresentation blah(@NotNull @Valid ValidRepresentation representation,
                                    @QueryParam("somethingelse") String xer) {
        return new ValidRepresentation();
    }

    @POST
    @Path("fooValidated")
    @Validated
    @Valid
    public ValidRepresentation blahValidated(@Validated @Valid ValidRepresentation representation) {
        return new ValidRepresentation();
    }

    @POST
    @Path("simpleEntity")
    public String simpleEntity(@Length(min = 3, max = 5) String name) {
        return name;
    }

    @GET
    @Path("bar")
    @Length(max = 3)
    public String blaze(@QueryParam("name") @Length(min = 3) String name) {
        return name;
    }

    @GET
    @Path("paramValidation")
    public Long paramValidation(@NotNull(payload = Unwrapping.Skip.class)
                                @Min(2) @Max(5)
                                @QueryParam("length") LongParam length) {
        return length.get();
    }

    @GET
    @Path("messageValidation")
    public Long messageValidation(@NotNull(payload = Unwrapping.Skip.class)
                                  @Min(value = 2, message = "The value ${validatedValue} is less then {value}")
                                  @QueryParam("length") LongParam length) {
        return length.get();
    }

    @GET
    @Path("barter")
    public String isnt(@QueryParam("name") @Length(min = 3) NonEmptyStringParam name) {
        return name.get().orElse(null);
    }

    @POST
    @Path("validatedPartialExampleBoth")
    public PartialExample validatedPartialExampleBoth(
            @NotNull
            @Valid
            @Validated({Partial1.class, Partial2.class})
                    PartialExample obj
    ) {
        return obj;
    }

    @POST
    @Path("validExample")
    public Example validExample(@NotNull @Valid Example obj) {
        return obj;
    }

    @POST
    @Path("validExampleArray")
    public Example[] validExample(@Valid Example[] obj) {
        return obj;
    }

    @POST
    @Path("validExampleCollection")
    public Collection<Example> validExample(@Valid Collection<Example> obj) {
        return obj;
    }

    @POST
    @Path("validExampleMap")
    public Map<String, Example> validExample(@Valid Map<String, Example> obj) {
        return obj;
    }

    @POST
    @Path("validExampleSet")
    public Set<Example> validExample(@Valid Set<Example> obj) {
        return obj;
    }

    @POST
    @Path("validExampleList")
    public List<Example> validExample(@Valid List<Example> obj) {
        return obj;
    }

    @POST
    @Path("validatedPartialExample")
    public PartialExample validatedPartialExample(
            @NotNull(groups = Partial1.class)
            @Valid
            @Validated({Partial1.class})
                    PartialExample obj
    ) {
        return obj;
    }

    @POST
    @Path("validExampleEmbeddedList")
    public List<ListExample> validExampleEmbedded(@Valid List<ListExample> obj) {
        return obj;
    }

    @GET
    @Path("fhqwhgads")
    public String everybody(@QueryParam("num") @Min(3L) @NotNull Long param) {
        return param.toString();
    }

    @GET
    @Path("zoo")
    public String blazer(@Valid @BeanParam BeanParameter params) {
        return params.getName();
    }

    @GET
    @Path("sub-zoo")
    public String subBlazer(@Valid @BeanParam SubBeanParameter params) {
        return params.getName() + " " + params.getAddress();
    }

    @GET
    @Path("sub-group-zoo")
    public String subGroupBlazer(@Valid @Validated(Partial1.class) @BeanParam SubBeanParameter params) {
        return params.getName() + " " + params.getAddress();
    }

    @POST
    @Path("sub-valid-group-zoo")
    public String subValidGroupBlazer(
            @Valid @Validated(Partial1.class) @BeanParam SubBeanParameter params,
            @Valid @Validated(Partial1.class) ValidRepresentation entity) {
        return params.getName() + " " + params.getAddress() + " " + entity.getName();
    }

    @GET
    @Path("zoo2")
    public String blazerValidated(@Validated @Valid @BeanParam BeanParameter params) {
        return params.getName();
    }

    @GET
    @Path("head")
    public String heads(@HeaderParam("cheese") @NotEmpty String secretSauce) {
        return secretSauce;
    }

    @GET
    @Path("headCopy")
    public String heads(@QueryParam("cheese") @NotNull(payload = Unwrapping.Skip.class) IntParam secretSauce) {
        return secretSauce.get().toString();
    }

    @GET
    @Path("nullable-int-param")
    public String nullableIntParam(@QueryParam("num") @Max(3) IntParam secretSauce) {
        return secretSauce == null ? "I was null" : secretSauce.get().toString();
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

    @GET
    @Path("enumParam")
    public String enumParam(@NotNull @QueryParam("choice") Choice choice) {
        return choice.toString();
    }

    @GET
    @Path("selfValidatingBeanParam")
    public SelfValidatingClass selfValidating(@Valid @BeanParam SelfValidatingClass beanParameter) {
        return beanParameter;
    }

    @POST
    @Path("selfValidatingPayload")
    public SelfValidatingClass selfValidatingPayload(@Valid SelfValidatingClass payload) {
        return payload;
    }

    @GET
    @Path("intParam")
    public Integer intParam(@QueryParam("num") @Min(23) IntParam intParam) {
        return intParam.get();
    }

    @GET
    @Path("intParamNotNull")
    public Integer intParamNotNull(@QueryParam("num")
                                   @NotNull(payload = Unwrapping.Skip.class) @Min(23) IntParam intParam) {
        return intParam.get();
    }

    @GET
    @Path("intParamWithDefault")
    public Integer intParamWithDefault(@QueryParam("num") @DefaultValue("42") @Min(23) IntParam intParam) {
        return intParam.get();
    }

    @GET
    @Path("intParamWithOptionalInside")
    public Integer intParamWithOptionalInside(@QueryParam("num") @Min(23) IntParam intParam) {
        return Optional.ofNullable(intParam).orElse(new IntParam("42")).get();
    }

    @GET
    @Path("optionalInt")
    public int optionalInt(@QueryParam("num") @Min(23) OptionalInt optionalInt) {
        return optionalInt.orElse(42);
    }

    @GET
    @Path("optionalIntWithDefault")
    public int optionalIntWithDefault(@QueryParam("num") @DefaultValue("23") @Min(23) OptionalInt optionalInt) {
        return optionalInt.orElse(42);
    }

    @GET
    @Path("optionalInteger")
    public int optionalInteger(@QueryParam("num")
                               @Min(value = 23, payload = Unwrapping.Unwrap.class) Optional<Integer> optionalInt) {
        return optionalInt.orElse(42);
    }

    @GET
    @Path("optionalIntegerWithDefault")
    public int optionalIntegerWithDefault(@QueryParam("num") @DefaultValue("23")
                                          @Min(value = 23, payload = Unwrapping.Unwrap.class) Optional<Integer> optionalInt) {
        return optionalInt.orElse(42);
    }
}
