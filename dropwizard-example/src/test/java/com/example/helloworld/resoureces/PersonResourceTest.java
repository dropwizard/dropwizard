package com.example.helloworld.resoureces;

import com.codahale.dropwizard.testing.ResourceTest;
import com.example.helloworld.core.Person;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.resources.PersonResource;
import com.google.common.base.Optional;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codahale.dropwizard.testing.JsonHelpers.asJson;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PersonResourceTest extends ResourceTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    private final Person person = new Person("Luther Blissett", "Football Player");
    private final PersonDAO store = mock(PersonDAO.class);

    @Override
    protected void setUpResources() {
        when(store.findById(101l)).thenReturn(Optional.fromNullable(person));
        addResource(new PersonResource(store));
    }

    @Test
    public void getPerson() throws Exception {
        assertThat(client().resource("/people/101").get(String.class))
                .isEqualTo(asJson(person));
    }
}
