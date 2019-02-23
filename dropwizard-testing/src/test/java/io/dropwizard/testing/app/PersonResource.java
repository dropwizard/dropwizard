package io.dropwizard.testing.app;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.jersey.params.IntParam;
import io.dropwizard.validation.Validated;
import org.eclipse.jetty.io.EofException;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.groups.Default;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

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
    public List<Person> getPersonList(@PathParam("name") String name) {
        return Collections.singletonList(getPerson(name));
    }

    @GET
    @Timed
    @Path("/index")
    public Person getPersonWithIndex(@QueryParam("ind") @Min(0) IntParam index,
                                     @PathParam("name") String name) {
        return getPersonList(name).get(index.get());
    }

    @POST
    @Path("/runtime-exception")
    public Person exceptional(Map<String, String> mapper) throws Exception {
        throw new Exception("I'm an exception!");
    }

    @GET
    @Path("/eof-exception")
    public Person eofException() throws Exception {
        throw new EofException("I'm an eof exception!");
    }

    @POST
    @Path("/validation-groups-exception")
    public String validationGroupsException(
        @Valid @Validated(Partial1.class) @BeanParam BeanParameter params,
        @Valid @Validated(Default.class) byte[] entity) {
        return requireNonNull(params.age).toString() + entity.length;
    }

    public interface Partial1 { }
    public static class BeanParameter {
        @QueryParam("age")
        @Nullable
        public Integer age;
    }
}
