package io.dropwizard.testing.app;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.testing.Person;

import javax.validation.constraints.Min;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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

    @GET
    @Timed
    @Path("/list")
    public ImmutableList<Person> getPersonList(@PathParam("name") String name) {
        return ImmutableList.of(getPerson(name));
    }

    @GET
    @Timed
    @Path("/index")
    public Person getPersonWithIndex(@Min(0) @QueryParam("ind") IntParam index,
                                     @PathParam("name") String name) {
        return getPersonList(name).get(index.get());
    }
}
