package io.dropwizard.testing.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.Person;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ResourceTestRule}.
 */
public class PersonResourceTest {
    private static class DummyExceptionMapper implements ExceptionMapper<WebApplicationException> {
        @Override
        public Response toResponse(WebApplicationException e) {
            throw new UnsupportedOperationException();
        }
    }

    private static final PeopleStore PEOPLE_STORE = mock(PeopleStore.class);

    private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper()
            .registerModule(new GuavaModule());

    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new PersonResource(PEOPLE_STORE))
            .setMapper(OBJECT_MAPPER)
            .setClientConfigurator(clientConfig -> clientConfig.register(DummyExceptionMapper.class))
            .build();

    private final Person person = new Person("blah", "blah@example.com");

    @Before
    public void setup() {
        reset(PEOPLE_STORE);
        when(PEOPLE_STORE.fetchPerson(eq("blah"))).thenReturn(person);
    }

    @Test
    public void testGetPerson() {
        assertThat(RESOURCES.client().target("/person/blah").request()
                .get(Person.class))
                .isEqualTo(person);
        verify(PEOPLE_STORE).fetchPerson("blah");
    }

    @Test
    public void testGetImmutableListOfPersons() {
        assertThat(RESOURCES.client().target("/person/blah/list").request()
            .get(new GenericType<ImmutableList<Person>>() {
            })).isEqualTo(ImmutableList.of(person));
    }

    @Test
    public void testGetPersonWithQueryParam() {
        // Test to ensure that the dropwizard validator is registered so that
        // it can validate the "ind" IntParam.
        assertThat(RESOURCES.client().target("/person/blah/index")
            .queryParam("ind", 0).request()
            .get(Person.class))
            .isEqualTo(person);
        verify(PEOPLE_STORE).fetchPerson("blah");
    }

    @Test
    public void testDefaultConstraintViolation() {
        assertThat(RESOURCES.client().target("/person/blah/index")
            .queryParam("ind", -1).request()
            .get().readEntity(String.class))
            .isEqualTo("{\"errors\":[\"query param ind must be greater than or equal to 0\"]}");
    }

    @Test
    public void testDefaultJsonProcessingMapper() {
        assertThat(RESOURCES.client().target("/person/blah/runtime-exception")
            .request()
            .post(Entity.json("{ \"he: \"ho\"}"))
            .readEntity(String.class))
            .isEqualTo("{\"code\":400,\"message\":\"Unable to process JSON\"}");
    }

    @Test
    public void testDefaultExceptionMapper() {
        assertThat(RESOURCES.client().target("/person/blah/runtime-exception")
            .request()
            .post(Entity.json("{}"))
            .readEntity(String.class))
            .startsWith("{\"code\":500,\"message\":\"There was an error processing your request. It has been logged");
    }

    @Test
    public void testDefaultEofExceptionMapper() {
        assertThat(RESOURCES.client().target("/person/blah/eof-exception")
            .request()
            .get().getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testValidationGroupsException() {
        final Response resp = RESOURCES.client().target("/person/blah/validation-groups-exception")
            .request()
            .post(Entity.json("{}"));
        assertThat(resp.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(resp.readEntity(String.class))
            .isEqualTo("{\"code\":500,\"message\":\"Parameters must have the same" +
                " validation groups in validationGroupsException\"}");
    }

    @Test
    public void testCustomClientConfiguration() {
        assertThat(RESOURCES.client().getConfiguration().isRegistered(DummyExceptionMapper.class)).isTrue();
    }
}
