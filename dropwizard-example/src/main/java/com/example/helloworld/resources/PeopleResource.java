package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PeopleDAO;
import com.wordnik.swagger.core.Api;
import com.wordnik.swagger.core.ApiOperation;
import com.yammer.dropwizard.swagger.SwaggerResourceSupport;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/people.json")
@Api(value = "/people", description = "People Resource")
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource extends SwaggerResourceSupport {

    private final PeopleDAO peopleDAO;

    public PeopleResource(PeopleDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @POST
    @ApiOperation(value = "Create Person")
    public Person createPerson(Person person) {
        final long personId = peopleDAO.create(person);
        return peopleDAO.findById(personId);
    }

    @GET
    @ApiOperation(value = "List People")
    public List<Person> listPeople() {
        return peopleDAO.findAll();
    }

}
