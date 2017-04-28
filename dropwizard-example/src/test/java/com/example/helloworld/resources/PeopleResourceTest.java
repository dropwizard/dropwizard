package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PeopleResource}.
 */
@RunWith(MockitoJUnitRunner.class)
public class PeopleResourceTest {
    private static final PersonDAO PERSON_DAO = mock(PersonDAO.class);
    @ClassRule
    public static final ResourceTestRule RESOURCES = ResourceTestRule.builder()
            .addResource(new PeopleResource(PERSON_DAO))
            .build();
    @Captor
    private ArgumentCaptor<Person> personCaptor;
    private Person person;

    @Before
    public void setUp() {
        person = new Person();
        person.setFullName("Full Name");
        person.setJobTitle("Job Title");
    }

    @After
    public void tearDown() {
        reset(PERSON_DAO);
    }

    @Test
    public void createPerson() throws JsonProcessingException {
        when(PERSON_DAO.create(any(Person.class))).thenReturn(person);
        final Response response = RESOURCES.target("/people")
                .request(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(person, MediaType.APPLICATION_JSON_TYPE));

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.OK);
        verify(PERSON_DAO).create(personCaptor.capture());
        assertThat(personCaptor.getValue()).isEqualTo(person);
    }

    @Test
    public void listPeople() throws Exception {
        final ImmutableList<Person> people = ImmutableList.of(person);
        when(PERSON_DAO.findAll()).thenReturn(people);

        final List<Person> response = RESOURCES.target("/people")
            .request().get(new GenericType<List<Person>>() {
            });

        verify(PERSON_DAO).findAll();
        assertThat(response).containsAll(people);
    }
}
