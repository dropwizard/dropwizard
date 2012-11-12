package com.yammer.dropwizard.testing.tests.service;

import com.yammer.dropwizard.testing.ResourceTest;
import com.yammer.dropwizard.testing.tests.Person;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonResourceTest extends ResourceTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    private final Person person = new Person("blah", "blah@example.com");
    private final PeopleStore store = mock(PeopleStore.class);

    @Override
    protected void setUpResources() {
        when(store.fetchPerson(anyString())).thenReturn(person);
        addResource(new PersonResource(store));
    }

    @Test
    public void simpleResourceTest() throws Exception {
        assertThat(client().resource("/person/blah").get(Person.class))
                .isEqualTo(person);
    }
}
