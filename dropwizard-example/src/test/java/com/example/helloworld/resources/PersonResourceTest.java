package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.UniformInterfaceException;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PersonResource}
 */
public class PersonResourceTest {
    private static final PersonDAO dao = mock(PersonDAO.class);

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new PersonResource(dao))
            .build();

    private Person person;

    @Before
    public void setup() {
        person = new Person();
        person.setId(1L);
    }

    @After
    public void tearDown() {
        reset(dao);
    }

    @Test
    public void getPerson_success() {
        when(dao.findById(1L)).thenReturn(Optional.of(person));

        Person found = resources.client().resource("/people/1").get(Person.class);
        assertThat(found.getId()).isEqualTo(person.getId());

        verify(dao.findById(1L));
    }

    @Test(expected = UniformInterfaceException.class)
    public void getPerson_404() {
        when(dao.findById(2L)).thenReturn(Optional.<Person>absent());
        resources.client().resource("/people/2").get(Person.class);

        verify(dao.findById(2L));
    }
}
