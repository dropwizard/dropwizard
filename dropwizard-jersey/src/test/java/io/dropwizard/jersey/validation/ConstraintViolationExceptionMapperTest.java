package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.Example;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.ListExample;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.PartialExample;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

public class ConstraintViolationExceptionMapperTest extends AbstractJerseyTest {
    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .packages("io.dropwizard.jersey.validation")
                .register(new ValidatingResource2())
                .register(new HibernateValidationBinder(Validators.newValidator()));
    }

    @BeforeClass
    public static void init() {
        // Set default locale to English because some tests assert localized error messages
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterClass
    public static void shutdown() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @Before
    public void setUp() throws Exception {
        assumeThat(Locale.getDefault().getLanguage()).isEqualTo("en");
        super.setUp();
    }

    @Test
    public void postInvalidEntityIs422() {
        final Response response = target("/valid/foo").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"errors\":[\"name must not be empty\"]}");
    }

    @Test
    public void postNullEntityIs422() {
        final Response response = target("/valid/foo").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);

        String ret = "{\"errors\":[\"The request body must not be null\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void postInvalidatedEntityIs422() {
        final Response response = target("/valid/fooValidated").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"errors\":[\"name must not be empty\"]}");
    }

    @Test
    public void postInvalidInterfaceEntityIs422() {
        final Response response = target("/valid2/repr").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity("{\"name\": \"a\"}", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"query param interfaceVariable must not be null\"]}");
    }

    @Test
    public void returnInvalidEntityIs500() {
        final Response response = target("/valid/foo").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{ \"name\": \"Coda\" }", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"server response name must not be empty\"]}");
    }

    @Test
    public void returnInvalidatedEntityIs500() {
        final Response response = target("/valid/fooValidated").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{ \"name\": \"Coda\" }", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"server response name must not be empty\"]}");
    }

    @Test
    public void getInvalidReturnIs500() {
        // return value is too long and so will fail validation
        final Response response = target("/valid/bar")
                .queryParam("name", "dropwizard").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response length must be between 0 and 3\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidQueryParamsIs400() {
        // query parameter is too short and so will fail validation
        final Response response = target("/valid/bar")
                .queryParam("name", "hi").request().get();

        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"query param name length must be between 3 and 2147483647\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);

        // Send another request to trigger reflection cache
        final Response cache = target("/valid/bar")
                .queryParam("name", "hi").request().get();
        assertThat(cache.getStatus()).isEqualTo(400);
        assertThat(cache.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void cacheIsForParamNamesOnly() {
        // query parameter must not be null, and must be at least 3
        final Response response = target("/valid/fhqwhgads")
                .queryParam("num", 2).request().get();

        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"query param num must be greater than or equal to 3\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);

        // Send another request to trigger reflection cache. This one is invalid in a different way
        // and should get a different message.
        final Response cache = target("/valid/fhqwhgads").request().get();
        assertThat(cache.getStatus()).isEqualTo(400);
        ret = "{\"errors\":[\"query param num must not be null\"]}";
        assertThat(cache.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void postInvalidPrimitiveIs422() {
        // query parameter is too short and so will fail validation
        final Response response = target("/valid/simpleEntity")
                .request().post(Entity.json("hi"));

        assertThat(response.getStatus()).isEqualTo(422);

        String ret = "{\"errors\":[\"The request body length must be between 3 and 5\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidCustomTypeIs400() {
        // query parameter is too short and so will fail validation
        final Response response = target("/valid/barter")
                .queryParam("name", "hi").request().get();

        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"query param name length must be between 3 and 2147483647\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void nonEmptyString_succeeds_with_valid_string() {
        final Response response = target("/valid/barter")
                .queryParam("name", "Joe User")
                .request().get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("Joe User");
    }

    @Test
    public void nonEmptyString_succeeds_with_missing() {
        final Response response = target("/valid/barter").request().get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class)).isEqualTo("TEST");
    }

    @Test
    public void getInvalidBeanParamsIs400() {
        // bean parameter is too short and so will fail validation
        Response response = target("/valid/zoo")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("\"name must be Coda\"")
                .containsOnlyOnce("\"query param name must not be empty\"")
                .containsOnlyOnce("\"query param choice must not be null\"");
    }

    @Test
    public void getInvalidSubBeanParamsIs400() {
        final Response response = target("/valid/sub-zoo")
                .queryParam("address", "42 Wallaby Way")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("query param name must not be empty")
                .containsOnlyOnce("name must be Coda");
    }

    @Test
    public void getGroupSubBeanParamsIs400() {
        final Response response = target("/valid/sub-group-zoo")
            .queryParam("address", "42 WALLABY WAY")
            .queryParam("name", "Coda")
            .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("[\"address must not be uppercase\"]");
    }

    @Test
    public void postValidGroupsIs400() {
        final Response response = target("/valid/sub-valid-group-zoo")
            .queryParam("address", "42 WALLABY WAY")
            .queryParam("name", "Coda")
            .request()
            .post(Entity.json("{}"));
        assertThat(response.getStatus()).isEqualTo(400);

        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("[\"address must not be uppercase\"]");
    }

    @Test
    public void getInvalidatedBeanParamsIs400() {
        // bean parameter is too short and so will fail validation
        final Response response = target("/valid/zoo2")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("\"name must be Coda\"")
                .containsOnlyOnce("\"query param name must not be empty\"");
    }

    @Test
    public void getInvalidHeaderParamsIs400() {
        final Response response = target("/valid/head")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"header cheese must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidCookieParamsIs400() {
        final Response response = target("/valid/cooks")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"cookie user_id must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidPathParamsIs400() {
        final Response response = target("/valid/goods/11")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"path param id must be a well-formed email address\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidFormParamsIs400() {
        final Response response = target("/valid/form")
                .request().post(Entity.form(new Form()));
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"form field username must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void postInvalidMethodClassIs422() {
        final Response response = target("/valid/nothing")
                .request().post(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(422);

        String ret = "{\"errors\":[\"must have a false thing\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidNestedReturnIs500() {
        final Response response = target("/valid/nested").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response representation.name must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidNested2ReturnIs500() {
        final Response response = target("/valid/nested2").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response example must have a false thing\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidContextIs400() {
        final Response response = target("/valid/context").request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"context must not be null\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void getInvalidMatrixParamIs400() {
        final Response response = target("/valid/matrix")
                .matrixParam("bob", "").request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"matrix param bob must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    public void functionWithSameNameReturnDifferentErrors() {
        // This test is to make sure that functions with the same name and
        // number of parameters (but different parameter types), don't return
        // the same validation error due to any caching effects
        final Response response = target("/valid/head")
                .request().get();

        String ret = "{\"errors\":[\"header cheese must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);

        final Response response2 = target("/valid/headCopy")
                .request().get();
        assertThat(response2.readEntity(Long.class)).isEqualTo(0L);
    }

    @Test
    public void paramsCanBeValidatedWhenNull() {
        assertThat(target("/valid/nullable-int-param")
            .request().get().readEntity(String.class)).isEqualTo("Not a number");
    }

    @Test
    public void paramsCanBeUnwrappedAndValidated() {
        assertThat(target("/valid/nullable-int-param").queryParam("num", 4)
            .request().get().readEntity(String.class))
            .containsOnlyOnce("[\"query param num must be less than or equal to 3\"]");
    }

    @Test
    public void returnPartialValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExample")
                .request().post(Entity.json("{\"id\":1}"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(PartialExample.class).id)
                .isEqualTo(1);
    }

    @Test
    public void invalidNullPartialValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExample")
            .request().post(Entity.json(null));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The request body must not be null\"]}");
    }

    @Test
    public void invalidEntityExceptionForPartialValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExampleBoth")
                .request().post(Entity.json("{\"id\":1}"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"text must not be null\"]}");
    }

    @Test
    public void invalidNullPartialBothValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExampleBoth")
            .request().post(Entity.json(null));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The request body must not be null\"]}");
    }

    @Test
    public void returnPartialBothValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExampleBoth")
                .request().post(Entity.json("{\"id\":1,\"text\":\"hello Cemo\"}"));

        assertThat(response.getStatus()).isEqualTo(200);

        PartialExample ex = response.readEntity(PartialExample.class);
        assertThat(ex.id).isEqualTo(1);
        assertThat(ex.text).isEqualTo("hello Cemo");
    }

    @Test
    public void invalidEntityExceptionForInvalidRequestEntities() {
        final Response response = target("/valid/validExample")
                .request().post(Entity.json("{\"id\":-1}"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"id must be greater than or equal to 0\"]}");
    }

    @Test
    public void returnRequestEntities() {
        final Response response = target("/valid/validExample")
                .request().post(Entity.json("{\"id\":1}"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Example.class).id)
                .isEqualTo(1);
    }

    @Test
    public void returnRequestArrayEntities() {
        final Response response = target("/valid/validExampleArray")
                .request().post(Entity.json("[{\"id\":1}, {\"id\":2}]"));

        final Example ex1 = new Example();
        final Example ex2 = new Example();
        ex1.id = 1;
        ex2.id = 2;

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Example[].class))
                .containsExactly(ex1, ex2);
    }

    @Test
    public void invalidRequestCollectionEntities() {
        final Response response = target("/valid/validExampleCollection")
                .request().post(Entity.json("[{\"id\":-1}, {\"id\":-2}]"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .contains("id must be greater than or equal to 0",
                        "id must be greater than or equal to 0");
    }

    @Test
    public void invalidRequestSingleCollectionEntities() {
        final Response response = target("/valid/validExampleCollection")
                .request().post(Entity.json("[{\"id\":1}, {\"id\":-2}]"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("id must be greater than or equal to 0");
    }

    @Test
    public void returnRequestCollectionEntities() {
        final Response response = target("/valid/validExampleCollection")
                .request().post(Entity.json("[{\"id\":1}, {\"id\":2}]"));

        assertThat(response.getStatus()).isEqualTo(200);
        final Collection<Example> example =
            response.readEntity(new GenericType<Collection<Example>>() {
            });

        Example ex1 = new Example();
        Example ex2 = new Example();
        ex1.id = 1;
        ex2.id = 2;

        assertThat(example).containsOnly(ex1, ex2);
    }

    @Test
         public void invalidRequestSetEntities() {
        final Response response = target("/valid/validExampleSet")
                .request().post(Entity.json("[{\"id\":1}, {\"id\":-2}]"));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("id must be greater than or equal to 0");
    }

    @Test
    public void invalidRequestListEntities() {
        final Response response = target("/valid/validExampleList")
                .request().post(Entity.json("[{\"id\":-1}, {\"id\":-2}]"));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"id must be greater than or equal to 0\"," +
                        "\"id must be greater than or equal to 0\"]}");
    }

    @Test
    public void throwsAConstraintViolationExceptionForEmptyRequestEntities() {
        final Response response = target("/valid/validExample")
                .request().post(Entity.json(null));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The request body must not be null\"]}");
    }

    @Test
    public void returnsValidatedMapRequestEntities() {
        final Response response = target("/valid/validExampleMap")
                .request().post(Entity.json("{\"one\": {\"id\":1}, \"two\": {\"id\":2}}"));

        assertThat(response.getStatus()).isEqualTo(200);

        Map<String, Example> map = response.readEntity(new GenericType<Map<String, Example>>() {
        });
        assertThat(requireNonNull(map.get("one")).id).isEqualTo(1);
        assertThat(requireNonNull(map.get("two")).id).isEqualTo(2);
    }

    @Test
    public void invalidMapRequestEntities() {
        final Response response = target("/valid/validExampleMap")
                .request().post(Entity.json("{\"one\": {\"id\":-1}, \"two\": {\"id\":-2}}"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"id must be greater than or equal to 0\"," +
                        "\"id must be greater than or equal to 0\"]}");
    }

    @Test
    public void returnsValidatedEmbeddedListEntities() {
        final Response response = target("/valid/validExampleEmbeddedList")
                .request().post(Entity.json("[ {\"examples\": [ {\"id\":1 } ] } ]"));

        assertThat(response.getStatus()).isEqualTo(200);
        List<ListExample> res = response.readEntity(new GenericType<List<ListExample>>() {
        });
        assertThat(res).hasSize(1);
        assertThat(res.get(0).examples).hasSize(1);
        assertThat(res.get(0).examples.get(0).id).isEqualTo(1);
    }

    @Test
    public void invalidEmbeddedListEntities() {
        final Response response = target("/valid/validExampleEmbeddedList")
                .request().post(Entity.json("[ {\"examples\": [ {\"id\":1 } ] }, { } ]"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("examples must not be empty");
    }

    @Test
    public void testInvalidFieldQueryParam() {
        final Response response = target("/valid/bar")
            .queryParam("sort", "foo")
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("sortParam must match \\\"^(asc|desc)$\\\"");
    }

    @Test
    public void missingParameterMessageContainsParameterName() {
        final Response response = target("/valid/paramValidation")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Long.class)).isEqualTo(0L);
    }

    @Test
    public void emptyParameterMessageContainsParameterName() {
        final Response response = target("/valid/paramValidation")
            .queryParam("length", "")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Long.class)).isEqualTo(0L);
    }

    @Test
    public void maxMessageContainsParameterName() {
        final Response response = target("/valid/paramValidation")
            .queryParam("length", 50)
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param length must be less than or equal to 5");
    }

    @Test
    public void minMessageContainsParameterName() {
        final Response response = target("/valid/paramValidation")
            .queryParam("length", 1)
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param length must be greater than or equal to 2");
    }

    @Test
    public void paramClassPassesValidation() {
        final Response response = target("/valid/paramValidation")
            .queryParam("length", 3)
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void minCustomMessage() {
        final Response response = target("/valid/messageValidation")
            .queryParam("length", 1)
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param length The value 1 is less then 2");

        final Response response2 = target("/valid/messageValidation")
            .queryParam("length", 0)
            .request()
            .get();
        assertThat(response2.getStatus()).isEqualTo(400);
        assertThat(response2.readEntity(String.class))
            .containsOnlyOnce("query param length The value 0 is less then 2");
    }

    @Test
    public void notPresentEnumParameter() {
        final Response response = target("/valid/enumParam")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param choice must not be null");
    }

    @Test
    public void invalidEnumParameter() {
        final Response response = target("/valid/enumParam")
            .queryParam("choice", "invalid")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param choice must be one of [OptionA, OptionB, OptionC]");
    }

    @Test
    public void invalidBeanParamEnumParameter() {
        final Response response = target("/valid/zoo")
            .queryParam("choice", "invalid")
            .request().get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param choice must be one of [OptionA, OptionB, OptionC]");
    }

    @Test
    public void selfValidatingBeanParamInvalid() {
        final Response response = target("/valid/selfValidatingBeanParam")
            .queryParam("answer", 100)
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The answer is 42\"]}");
    }

    @Test
    public void selfValidatingBeanParamSuccess() {
        final Response response = target("/valid/selfValidatingBeanParam")
            .queryParam("answer", 42)
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"answer\":42}");
    }

    @Test
    public void selfValidatingPayloadInvalid() {
        final Response response = target("/valid/selfValidatingPayload")
            .request()
            .post(Entity.json("{\"answer\":100}"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The answer is 42\"]}");
    }

    @Test
    public void selfValidatingPayloadSuccess() {
        final String payload = "{\"answer\":42}";

        final Response response = target("/valid/selfValidatingPayload")
            .request()
            .post(Entity.json(payload));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class))
            .isEqualTo(payload);
    }

    @Test
    public void optionalInt_succeeds_with_missing() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    public void optionalInt_succeeds_with_empty_string() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    public void optionalInt_fails_with_constraint_vioalation() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    public void optionalInt_fails_with_string() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    public void optionalInt_succeeds() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }


    @Test
    public void optionalIntWithDefault_succeeds_with_missing() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(23);
    }

    @Test
    public void optionalIntWithDefault_succeeds_with_empty_string() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    public void optionalIntWithDefault_fails_with_constraint_violation() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    public void optionalIntWithDefault_succeeds_with_string() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    public void optionalIntWithDefault_succeeds() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }

    @Test
    public void optionalInteger_succeeds_with_missing() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    public void optionalInteger_succeeds_with_empty_string() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    public void optionalInteger_fails_with_constraint_vioalation() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    public void optionalInteger_fails_with_string() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.hasEntity()).isFalse();
    }

    @Test
    public void optionalInteger_succeeds() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }


    @Test
    public void optionalIntegerWithDefault_succeeds_with_missing() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(23);
    }

    @Test
    public void optionalIntegerWithDefault_succeeds_with_empty_string() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    public void optionalIntegerWithDefault_fails_with_constraint_vioalation() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    public void optionalIntegerWithDefault_fails_with_string() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.hasEntity()).isFalse();
    }

    @Test
    public void optionalIntegerWithDefault_succeeds() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }
}
