package io.dropwizard.testing.app;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.testing.Person;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/person/{name}")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {
    private final PeopleStore store;

    public PersonResource(PeopleStore store) {
        this.store = store;
    }

    @GET
    @Timed
    public Person getPerson(@PathParam("name") String name) {
        return store.fetchPerson(name);
    }
}
