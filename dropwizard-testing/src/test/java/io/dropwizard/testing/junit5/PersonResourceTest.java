package io.dropwizard.testing.junit5;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.app.PeopleStore;
import io.dropwizard.testing.app.Person;
import io.dropwizard.testing.app.PersonResource;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests {@link ResourceTestRule}.
 */
@ExtendWith(DropwizardExtensionsSupport.class)
public class PersonResourceTest {
    private static class DummyExceptionMapper implements ExceptionMapper<WebApplicationException> {
        @Override
        public Response toResponse(WebApplicationException e) {
            throw new UnsupportedOperationException();
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = Jackson.newObjectMapper()
        .registerModule(new GuavaModule());

    private PeopleStore peopleStore = mock(PeopleStore.class);
    private ResourceExtension resources = ResourceExtension.builder()
        .addResource(new PersonResource(peopleStore))
        .setMapper(OBJECT_MAPPER)
        .setClientConfigurator(clientConfig -> clientConfig.register(DummyExceptionMapper.class))
        .build();

    private Person person = new Person("blah", "blah@example.com");

    @BeforeEach
    public void setup() {
        when(peopleStore.fetchPerson("blah")).thenReturn(person);
    }

    @Test
    public void testGetPerson() {
        assertThat(resources.target("/person/blah").request()
            .get(Person.class))
            .isEqualTo(person);
        verify(peopleStore).fetchPerson("blah");
    }

    @Test
    public void testGetImmutableListOfPersons() {
        assertThat(resources.target("/person/blah/list").request().get(new GenericType<List<Person>>() {
            })).containsOnly(person);
    }

    @Test
    public void testGetPersonWithQueryParam() {
        // Test to ensure that the dropwizard validator is registered so that
        // it can validate the "ind" IntParam.
        assertThat(resources.target("/person/blah/index")
            .queryParam("ind", 0).request()
            .get(Person.class))
            .isEqualTo(person);
        verify(peopleStore).fetchPerson("blah");
    }

    @Test
    public void testDefaultConstraintViolation() {
        assertThat(resources.target("/person/blah/index")
            .queryParam("ind", -1).request()
            .get().readEntity(String.class))
            .isEqualTo("{\"errors\":[\"query param ind must be greater than or equal to 0\"]}");
    }

    @Test
    public void testDefaultJsonProcessingMapper() {
        assertThat(resources.target("/person/blah/runtime-exception")
            .request()
            .post(Entity.json("{ \"he: \"ho\"}"))
            .readEntity(String.class))
            .isEqualTo("{\"code\":400,\"message\":\"Unable to process JSON\"}");
    }

    @Test
    public void testDefaultExceptionMapper() {
        assertThat(resources.target("/person/blah/runtime-exception")
            .request()
            .post(Entity.json("{}"))
            .readEntity(String.class))
            .startsWith("{\"code\":500,\"message\":\"There was an error processing your request. It has been logged");
    }

    @Test
    public void testDefaultEofExceptionMapper() {
        assertThat(resources.target("/person/blah/eof-exception")
            .request()
            .get().getStatus())
            .isEqualTo(Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testValidationGroupsException() {
        final Response resp = resources.target("/person/blah/validation-groups-exception")
            .request()
            .post(Entity.json("{}"));
        assertThat(resp.getStatus()).isEqualTo(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        assertThat(resp.readEntity(String.class))
            .isEqualTo("{\"code\":500,\"message\":\"Parameters must have the same" +
                " validation groups in validationGroupsException\"}");
    }

    @Test
    public void testCustomClientConfiguration() {
        assertThat(resources.client().getConfiguration().isRegistered(DummyExceptionMapper.class)).isTrue();
    }
}
