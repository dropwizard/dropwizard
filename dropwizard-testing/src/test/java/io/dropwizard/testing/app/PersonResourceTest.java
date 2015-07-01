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

import javax.ws.rs.core.GenericType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests {@link ResourceTestRule}.
 */
public class PersonResourceTest {
    private static final PeopleStore dao = mock(PeopleStore.class);

    private static final ObjectMapper mapper = Jackson.newObjectMapper()
            .registerModule(new GuavaModule());

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new PersonResource(dao))
            .setMapper(mapper)
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
}
