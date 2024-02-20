package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.Example;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.ListExample;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProviderTest.PartialExample;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

class ConstraintViolationExceptionMapperTest extends AbstractJerseyTest {
    private static class LoggingExceptionMapperBinder extends AbstractBinder {
        protected void configure() {
            this.bind(new LoggingExceptionMapper<Throwable>() {
            }).to(ExceptionMapper.class);
        }
    }

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();

    @Override
    protected Application configure() {
        return DropwizardResourceConfig.forTesting()
                .packages("io.dropwizard.jersey.validation")
                .register(new ValidatingResource2())
                .register(new LoggingExceptionMapperBinder())
                .register(new HibernateValidationBinder(Validators.newValidator()));
    }

    @BeforeAll
    static void init() {
        // Set default locale to English because some tests assert localized error messages
        Locale.setDefault(Locale.ENGLISH);
    }

    @AfterAll
    static void shutdown() {
        Locale.setDefault(DEFAULT_LOCALE);
    }

    @Test
    void postInvalidEntityIs422() {
        final Response response = target("/valid/foo").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"errors\":[\"name must not be empty\"]}");
    }

    @Test
    void postNullEntityIs422() {
        final Response response = target("/valid/foo").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(null, MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);

        String ret = "{\"errors\":[\"The request body must not be null\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void postInvalidatedEntityIs422() {
        final Response response = target("/valid/fooValidated").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{}", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class)).isEqualTo("{\"errors\":[\"name must not be empty\"]}");
    }

    @Test
    void postInvalidInterfaceEntityIs422() {
        final Response response = target("/valid2/repr").request(MediaType.APPLICATION_JSON)
            .post(Entity.entity("{\"name\": \"a\"}", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"query param interfaceVariable must not be null\"]}");
    }

    @Test
    void returnInvalidEntityIs500() {
        final Response response = target("/valid/foo").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{ \"name\": \"Coda\" }", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"server response name must not be empty\"]}");
    }

    @Test
    void returnInvalidatedEntityIs500() {
        final Response response = target("/valid/fooValidated").request(MediaType.APPLICATION_JSON)
                .post(Entity.entity("{ \"name\": \"Coda\" }", MediaType.APPLICATION_JSON));
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"server response name must not be empty\"]}");
    }

    @Test
    void getInvalidReturnIs500() {
        // return value is too long and so will fail validation
        final Response response = target("/valid/bar")
                .queryParam("name", "dropwizard").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response length must be between 0 and 3\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidQueryParamsIs400() {
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
    void cacheIsForParamNamesOnly() {
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
    void postInvalidPrimitiveIs422() {
        // query parameter is too short and so will fail validation
        final Response response = target("/valid/simpleEntity")
                .request().post(Entity.json("hi"));

        assertThat(response.getStatus()).isEqualTo(422);

        String ret = "{\"errors\":[\"The request body length must be between 3 and 5\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidCustomTypeIs400() {
        // query parameter is too short and so will fail validation
        final Response response = target("/valid/barter")
                .queryParam("name", "hi").request().get();

        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"query param name length must be between 3 and 2147483647\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidBeanParamsIs400() {
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
    void getInvalidSubBeanParamsIs400() {
        final Response response = target("/valid/sub-zoo")
                .queryParam("address", "42 Wallaby Way")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("query param name must not be empty")
                .containsOnlyOnce("name must be Coda");
    }

    @Test
    void getGroupSubBeanParamsIs400() {
        final Response response = target("/valid/sub-group-zoo")
            .queryParam("address", "42 WALLABY WAY")
            .queryParam("name", "Coda")
            .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("[\"address must not be uppercase\"]");
    }

    @Test
    void postValidGroupsIs400() {
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
    void getInvalidatedBeanParamsIs400() {
        // bean parameter is too short and so will fail validation
        final Response response = target("/valid/zoo2")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("\"name must be Coda\"")
                .containsOnlyOnce("\"query param name must not be empty\"");
    }

    @Test
    void getInvalidHeaderParamsIs400() {
        final Response response = target("/valid/head")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"header cheese must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidCookieParamsIs400() {
        final Response response = target("/valid/cooks")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"cookie user_id must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidPathParamsIs400() {
        final Response response = target("/valid/goods/11")
                .request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"path param id must be a well-formed email address\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidFormParamsIs400() {
        final Response response = target("/valid/form")
                .request().post(Entity.form(new Form()));
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"form field username must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void postInvalidMethodClassIs422() {
        final Response response = target("/valid/nothing")
                .request().post(Entity.entity("{}", MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(422);

        String ret = "{\"errors\":[\"must have a false thing\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidNestedReturnIs500() {
        final Response response = target("/valid/nested").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response representation.name must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidNested2ReturnIs500() {
        final Response response = target("/valid/nested2").request().get();
        assertThat(response.getStatus()).isEqualTo(500);

        String ret = "{\"errors\":[\"server response example must have a false thing\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidContextIs400() {
        final Response response = target("/valid/context").request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"context must not be null\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void getInvalidMatrixParamIs400() {
        final Response response = target("/valid/matrix")
                .matrixParam("bob", "").request().get();
        assertThat(response.getStatus()).isEqualTo(400);

        String ret = "{\"errors\":[\"matrix param bob must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);
    }

    @Test
    void functionWithSameNameReturnDifferentErrors() {
        // This test is to make sure that functions with the same name and
        // number of parameters (but different parameter types), don't return
        // the same validation error due to any caching effects
        final Response response = target("/valid/head")
                .request().get();

        String ret = "{\"errors\":[\"header cheese must not be empty\"]}";
        assertThat(response.readEntity(String.class)).isEqualTo(ret);

        final Response response2 = target("/valid/headCopy")
                .request().get();
        assertThat(response2.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param cheese is not a number.\"}");
    }

    @Test
    void paramsCanBeUnwrappedAndValidated() {
        final Response response = target("/valid/nullable-int-param")
                .queryParam("num", 4)
                .request()
                .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"query param num must be less than or equal to 3\"]}");
    }

    @Test
    void returnPartialValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExample")
                .request().post(Entity.json("{\"id\":1}"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(PartialExample.class).id)
                .isEqualTo(1);
    }

    @Test
    void invalidNullPartialValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExample")
            .request().post(Entity.json(null));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The request body must not be null\"]}");
    }

    @Test
    void invalidEntityExceptionForPartialValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExampleBoth")
                .request().post(Entity.json("{\"id\":1}"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"text must not be null\"]}");
    }

    @Test
    void invalidNullPartialBothValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExampleBoth")
            .request().post(Entity.json(null));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The request body must not be null\"]}");
    }

    @Test
    void returnPartialBothValidatedRequestEntities() {
        final Response response = target("/valid/validatedPartialExampleBoth")
                .request().post(Entity.json("{\"id\":1,\"text\":\"hello Cemo\"}"));

        assertThat(response.getStatus()).isEqualTo(200);

        PartialExample ex = response.readEntity(PartialExample.class);
        assertThat(ex.id).isEqualTo(1);
        assertThat(ex.text).isEqualTo("hello Cemo");
    }

    @Test
    void invalidEntityExceptionForInvalidRequestEntities() {
        final Response response = target("/valid/validExample")
                .request().post(Entity.json("{\"id\":-1}"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"id must be greater than or equal to 0\"]}");
    }

    @Test
    void returnRequestEntities() {
        final Response response = target("/valid/validExample")
                .request().post(Entity.json("{\"id\":1}"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Example.class).id)
                .isEqualTo(1);
    }

    @Test
    void returnRequestArrayEntities() {
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
    void invalidRequestCollectionEntities() {
        final Response response = target("/valid/validExampleCollection")
                .request().post(Entity.json("[{\"id\":-1}, {\"id\":-2}]"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .contains("id must be greater than or equal to 0",
                        "id must be greater than or equal to 0");
    }

    @Test
    void invalidRequestSingleCollectionEntities() {
        final Response response = target("/valid/validExampleCollection")
                .request().post(Entity.json("[{\"id\":1}, {\"id\":-2}]"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("id must be greater than or equal to 0");
    }

    @Test
    void returnRequestCollectionEntities() {
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
         void invalidRequestSetEntities() {
        final Response response = target("/valid/validExampleSet")
                .request().post(Entity.json("[{\"id\":1}, {\"id\":-2}]"));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("id must be greater than or equal to 0");
    }

    @Test
    void invalidRequestListEntities() {
        final Response response = target("/valid/validExampleList")
                .request().post(Entity.json("[{\"id\":-1}, {\"id\":-2}]"));
        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"id must be greater than or equal to 0\"," +
                        "\"id must be greater than or equal to 0\"]}");
    }

    @Test
    void throwsAConstraintViolationExceptionForEmptyRequestEntities() {
        final Response response = target("/valid/validExample")
                .request().post(Entity.json(null));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The request body must not be null\"]}");
    }

    @Test
    void returnsValidatedMapRequestEntities() {
        final Response response = target("/valid/validExampleMap")
                .request().post(Entity.json("{\"one\": {\"id\":1}, \"two\": {\"id\":2}}"));

        assertThat(response.getStatus()).isEqualTo(200);

        Map<String, Example> map = response.readEntity(new GenericType<Map<String, Example>>() {
        });
        assertThat(requireNonNull(map.get("one")).id).isEqualTo(1);
        assertThat(requireNonNull(map.get("two")).id).isEqualTo(2);
    }

    @Test
    void invalidMapRequestEntities() {
        final Response response = target("/valid/validExampleMap")
                .request().post(Entity.json("{\"one\": {\"id\":-1}, \"two\": {\"id\":-2}}"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"id must be greater than or equal to 0\"," +
                        "\"id must be greater than or equal to 0\"]}");
    }

    @Test
    void returnsValidatedEmbeddedListEntities() {
        final Response response = target("/valid/validExampleEmbeddedList")
                .request().post(Entity.json("[ {\"examples\": [ {\"id\":1 } ] } ]"));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(new GenericType<List<ListExample>>() {
        }))
            .singleElement()
            .extracting("examples")
            .asInstanceOf(InstanceOfAssertFactories.LIST)
            .singleElement()
            .extracting("id")
            .isEqualTo(1);
    }

    @Test
    void invalidEmbeddedListEntities() {
        final Response response = target("/valid/validExampleEmbeddedList")
                .request().post(Entity.json("[ {\"examples\": [ {\"id\":1 } ] }, { } ]"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
                .containsOnlyOnce("examples must not be empty");
    }

    @Test
    void testInvalidFieldQueryParam() {
        final Response response = target("/valid/bar")
            .queryParam("sort", "foo")
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("sortParam must match \\\"^(asc|desc)$\\\"");
    }

    @Test
    void missingParameterMessageContainsParameterName() {
        final Response response = target("/valid/paramValidation")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param length is not a number.");
    }

    @Test
    void emptyParameterMessageContainsParameterName() {
        final Response response = target("/valid/paramValidation")
            .queryParam("length", "")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param length is not a number.\"}");
    }

    @Test
    void maxMessageContainsParameterName() {
        final Response response = target("/valid/paramValidation")
            .queryParam("length", 50)
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param length must be less than or equal to 5");
    }

    @Test
    void minMessageContainsParameterName() {
        final Response response = target("/valid/paramValidation")
            .queryParam("length", 1)
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param length must be greater than or equal to 2");
    }

    @Test
    void paramClassPassesValidation() {
        final Response response = target("/valid/paramValidation")
            .queryParam("length", 3)
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void minCustomMessage() {
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
    void notPresentEnumParameter() {
        final Response response = target("/valid/enumParam")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param choice must not be null");
    }

    @Test
    void invalidEnumParameter() {
        final Response response = target("/valid/enumParam")
            .queryParam("choice", "invalid")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param choice must be one of [OptionA, OptionB, OptionC]");
    }

    @Test
    void invalidBeanParamEnumParameter() {
        final Response response = target("/valid/zoo")
            .queryParam("choice", "invalid")
            .request().get();
        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .containsOnlyOnce("query param choice must be one of [OptionA, OptionB, OptionC]");
    }

    @Test
    void selfValidatingBeanParamInvalid() {
        final Response response = target("/valid/selfValidatingBeanParam")
            .queryParam("answer", 100)
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The answer is 42\"]}");
    }

    @Test
    void selfValidatingBeanParamSuccess() {
        final Response response = target("/valid/selfValidatingBeanParam")
            .queryParam("answer", 42)
            .request()
            .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"answer\":42}");
    }

    @Test
    void selfValidatingPayloadInvalid() {
        final Response response = target("/valid/selfValidatingPayload")
            .request()
            .post(Entity.json("{\"answer\":100}"));

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"errors\":[\"The answer is 42\"]}");
    }

    @Test
    void selfValidatingPayloadSuccess() {
        final String payload = "{\"answer\":42}";

        final Response response = target("/valid/selfValidatingPayload")
            .request()
            .post(Entity.json(payload));

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(String.class))
            .isEqualTo(payload);
    }

    @Test
    void longParam_fails_with_null() {
        final Response response = target("/valid/longParam")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void longParam_fails_with_emptyString() {
        final Response response = target("/valid/longParam")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void longParam_fails_with_constraint_violation() {
        final Response response = target("/valid/longParam")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void longParam_fails_with_string() {
        final Response response = target("/valid/longParam")
                .queryParam("num", "string")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }


    @Test
    void longParam_succeeds() {
        final Response response = target("/valid/longParam")
                .queryParam("num", 42)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void longParamNotNull_fails_with_null() {
        final Response response = target("/valid/longParamNotNull")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void longParamNotNull_fails_with_empty_string() {
        final Response response = target("/valid/longParamNotNull")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void longParamNotNull_fails_with_constraint_violation() {
        final Response response = target("/valid/longParamNotNull")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void longParamNotNull_fails_with_string() {
        final Response response = target("/valid/longParamNotNull")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void longParamNotNull_succeeds() {
        final Response response = target("/valid/longParamNotNull")
                .queryParam("num", 42)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void longParamWithDefault_succeeds_with_null() {
        final Response response = target("/valid/longParamWithDefault")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void longParamWithDefault_fails_with_empty_string() {
        final Response response = target("/valid/longParamWithDefault")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Long.class)).isEqualTo(42L);
    }

    @Test
    void longParamWithDefault_fails_with_constraint_violation() {
        final Response response = target("/valid/longParamWithDefault")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void longParamWithDefault_fails_with_string() {
        final Response response = target("/valid/longParamWithDefault")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void longParamWithDefault_succeeds() {
        final Response response = target("/valid/longParamWithDefault")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }

    @Test
    void intParam_fails_with_null() {
        final Response response = target("/valid/intParam")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParam_fails_with_emptyString() {
        final Response response = target("/valid/intParam")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParam_fails_with_constraint_violation() {
        final Response response = target("/valid/intParam")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void intParam_fails_with_string() {
        final Response response = target("/valid/intParam")
                .queryParam("num", "string")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }


    @Test
    void intParam_succeeds() {
        final Response response = target("/valid/intParam")
                .queryParam("num", 42)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void intParamNotNull_fails_with_null() {
        final Response response = target("/valid/intParamNotNull")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParamNotNull_fails_with_empty_string() {
        final Response response = target("/valid/intParamNotNull")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParamNotNull_fails_with_constraint_violation() {
        final Response response = target("/valid/intParamNotNull")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void intParamNotNull_fails_with_string() {
        final Response response = target("/valid/intParamNotNull")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParamNotNull_succeeds() {
        final Response response = target("/valid/intParamNotNull")
                .queryParam("num", 42)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void intParamWithDefault_succeeds_with_null() {
        final Response response = target("/valid/intParamWithDefault")
                .queryParam("num")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void intParamWithDefault_fails_with_empty_string() {
        final Response response = target("/valid/intParamWithDefault")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void intParamWithDefault_fails_with_constraint_violation() {
        final Response response = target("/valid/intParamWithDefault")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void intParamWithDefault_fails_with_string() {
        final Response response = target("/valid/intParamWithDefault")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParamWithDefault_succeeds() {
        final Response response = target("/valid/intParamWithDefault")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }

    @Test
    void intParamWithOptionalInside_fails_with_missing() {
        final Response response = target("/valid/intParamWithOptionalInside")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParamWithOptionalInside_fails_with_empty_string() {
        final Response response = target("/valid/intParamWithOptionalInside")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParamWithOptionalInside_fails_with_constraint_violation() {
        final Response response = target("/valid/intParamWithOptionalInside")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void intParamWithOptionalInside_fails_with_string() {
        final Response response = target("/valid/intParamWithOptionalInside")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"query param num is not a number.\"}");
    }

    @Test
    void intParamWithOptionalInside_succeeds() {
        final Response response = target("/valid/intParamWithOptionalInside")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }

    @Test
    void optionalInt_succeeds_with_missing() {
        final Response response = target("/valid/optionalInt")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void optionalInt_succeeds_with_empty_string() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void optionalInt_fails_with_constraint_violation() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void optionalInt_fails_with_string() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void optionalInt_succeeds() {
        final Response response = target("/valid/optionalInt")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }


    @Test
    void optionalIntWithDefault_succeeds_with_missing() {
        final Response response = target("/valid/optionalIntWithDefault")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(23);
    }

    @Test
    void optionalIntWithDefault_succeeds_with_empty_string() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void optionalIntWithDefault_fails_with_constraint_violation() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void optionalIntWithDefault_fails_with_string() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void optionalIntWithDefault_succeeds() {
        final Response response = target("/valid/optionalIntWithDefault")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }

    @Test
    void optionalInteger_succeeds_with_missing() {
        final Response response = target("/valid/optionalInteger")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void optionalInteger_succeeds_with_empty_string() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void optionalInteger_fails_with_constraint_violation() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void optionalInteger_fails_with_string() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"code\":404,\"message\":\"HTTP 404 Not Found\"}");
    }

    @Test
    void optionalInteger_succeeds() {
        final Response response = target("/valid/optionalInteger")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }


    @Test
    void optionalIntegerWithDefault_succeeds_with_missing() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(23);
    }

    @Test
    void optionalIntegerWithDefault_succeeds_with_empty_string() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num", "")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(42);
    }

    @Test
    void optionalIntegerWithDefault_fails_with_constraint_violation() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num", 5)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"query param num must be greater than or equal to 23\"]}");
    }

    @Test
    void optionalIntegerWithDefault_fails_with_string() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num", "test")
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(404);
        assertThat(response.readEntity(String.class))
            .isEqualTo("{\"code\":404,\"message\":\"HTTP 404 Not Found\"}");
    }

    @Test
    void optionalIntegerWithDefault_succeeds() {
        final Response response = target("/valid/optionalIntegerWithDefault")
                .queryParam("num", 30)
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.readEntity(Integer.class)).isEqualTo(30);
    }
}
