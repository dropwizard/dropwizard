package io.dropwizard.testing.app;

import io.dropwizard.testing.Person;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

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
        when(dao.fetchPerson(eq("blah"))).thenReturn(person);
    }

    @Test
    public void testGetPerson() {
        assertThat(resources.client().target("/person/blah").request()
                .get(Person.class))
                .isEqualTo(person);
        verify(dao).fetchPerson("blah");
    }
}
