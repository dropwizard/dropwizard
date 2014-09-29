package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.google.common.base.Optional;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PersonResource}.
 */
public class PersonResourceTest {
    private static final PersonDAO DAO = mock(PersonDAO.class);
    @ClassRule
    public static final ResourceTestRule RULE = ResourceTestRule.builder()
            .addResource(new PersonResource(DAO))
            .build();
    private Person person;

    @Before
    public void setup() {
        person = new Person();
        person.setId(1L);
    }

    @After
    public void tearDown() {
        reset(DAO);
    }

    @Test
    public void getPersonSuccess() {
        when(DAO.findById(1L)).thenReturn(Optional.of(person));

        Person found = RULE.client().target("/people/1").request().get(Person.class);

        assertThat(found.getId()).isEqualTo(person.getId());
        verify(DAO).findById(1L);
    }

    @Test
    public void getPersonNotFound() {
        when(DAO.findById(2L)).thenReturn(Optional.<Person>absent());
        final Response response = RULE.client().target("/people/2").request().get();

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.NOT_FOUND);
        verify(DAO).findById(2L);
    }
}
