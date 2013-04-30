package com.example.helloworld.resoureces;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.resources.PeopleResource;
import com.google.common.collect.Lists;
import com.yammer.dropwizard.testing.ResourceTest;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.yammer.dropwizard.testing.JsonHelpers.asJson;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class PeopleResourceTest extends ResourceTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    private final Person person = new Person("Luther Blissett", "Football Player");
    private final PersonDAO store = mock(PersonDAO.class);

    @Override
    protected void setUpResources() {
        when(store.findAll()).thenReturn(Lists.newArrayList(person));
        addResource(new PeopleResource(store));
    }

    @Test
    public void createPerson() throws  Exception {
        client().resource("/people").type(MediaType.APPLICATION_JSON_TYPE).post(asJson(person));
        verify(store).create(any(Person.class));
    }

    @Test
    public void getPeople() throws Exception {
        assertThat(client().resource("/people").get(String.class))
                .isEqualTo(asJson(Lists.newArrayList(person)));
    }
}
