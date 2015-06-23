package io.dropwizard.testing.app;

import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.Person;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests {@link ResourceTestRule}.
 */
public class PersonResourceTest {
    private static final PeopleStore dao = mock(PeopleStore.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new PersonResource(dao))
            .build();

    private final Person person = new Person("blah", "blah@example.com");

    @Before
    public void setup() {
        reset(dao);
        when(dao.fetchPerson(eq("blah"))).thenReturn(person);
    }

    @Test
    public void testGetPerson() {
        assertThat(resources.client().target("/person/blah").request()
                .get(Person.class))
                .isEqualTo(person);
        verify(dao).fetchPerson("blah");
    }

    @Test
    public void testGetImmutableListOfPersons() {
        assertThat(resources.client().target("/person/blah/list").request()
                .get(new GenericType<ImmutableList<Person>>() {}))
                .isEqualTo(ImmutableList.of(person));
    }

    @Test
    public void testThatJsonExceptionMapperIsRegistered() {
        final Response resp = resources.client().target("/person/blah").request()
                .post(Entity.json("{"));

        assertThat(resp.getStatus()).isEqualTo(400);
        assertThat(resp.readEntity(String.class))
                .isEqualTo("{\"code\":400,\"message\":\"Unable to process JSON\"}");
    }

    @Test
    public void testThatLoggingExceptionIsRegistered() {
        final Response resp = resources.client().target("/person/blah").request()
                .post(Entity.json("{\"name\": \"Coda\"}"));

        assertThat(resp.getStatus()).isEqualTo(500);
        assertThat(resp.readEntity(String.class))
                .startsWith("{\"code\":500,\"message\":\"There was an error processing" +
                        " your request. It has been logged");
    }

    @Test
    public void testThatConstraintExceptionMapperIsRegistered() {
        final Response resp = resources.client().target("/person/blah").request()
                .post(Entity.json("{}"));

        assertThat(resp.getStatus()).isEqualTo(422);
        assertThat(resp.readEntity(String.class))
                .isEqualTo("{\"errors\":[\"name may not be empty\"]}");
    }
}
