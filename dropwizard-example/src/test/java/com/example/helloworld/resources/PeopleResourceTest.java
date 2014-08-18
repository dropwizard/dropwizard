package com.example.helloworld.resources;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * Unit tests for {@link PeopleResource}.
 */
public class PeopleResourceTest {
    @Captor ArgumentCaptor<Person> personCaptor;
    private static final PersonDAO dao = mock(PersonDAO.class);

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new PeopleResource(dao))
            .build();

    private Person person;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        person = new Person();
        person.setFullName("Full Name");
        person.setJobTitle("job title");
    }

    @After
    public void tearDown() {
        reset(dao);
    }

    @Test
    public void createPerson() throws JsonProcessingException {
        resources.client().resource("/people")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .post(person);

        verify(dao).create(personCaptor.capture());
        assertThat(MAPPER.writeValueAsString(personCaptor.getValue()))
                .isEqualTo(MAPPER.writeValueAsString(person));
    }

    @Test
    public void listPeople() throws Exception {
        when(dao.findAll()).thenReturn(ImmutableList.of(person));
        List people = resources.client().resource("/people").get(List.class);

        assertThat(MAPPER.writeValueAsString(people))
                .isEqualTo(MAPPER.writeValueAsString(ImmutableList.of(person)));
        verify(dao).findAll();
    }
}