package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PeopleDAO;
import com.wordnik.swagger.core.Api;
import com.wordnik.swagger.core.ApiOperation;
import com.wordnik.swagger.core.ApiParam;
import com.yammer.dropwizard.swagger.SwaggerResource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/people.json")
@Api("/people")
@Produces(MediaType.APPLICATION_JSON)
public class PeopleResource extends SwaggerResource {

    private final PeopleDAO peopleDAO;

    public PeopleResource(PeopleDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @POST
    @ApiOperation(
            value = "Create Person",
            responseClass = "com.example.helloworld.core.Person")
    public Person createPerson(@ApiParam(required = true) Person person) {
        final long personId = peopleDAO.create(person);
        return peopleDAO.findById(personId);
    }

    @GET
    @ApiOperation(
            value = "List People",
            responseClass = "com.example.helloworld.core.Person",
            multiValueResponse = true)
    public List<Person> listPeople() {
        return peopleDAO.findAll();
    }

}
