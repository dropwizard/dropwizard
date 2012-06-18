package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PeopleDAO;
import com.wordnik.swagger.core.Api;
import com.wordnik.swagger.core.ApiOperation;
import com.wordnik.swagger.core.ApiParam;
import com.yammer.dropwizard.jersey.params.LongParam;
import com.yammer.dropwizard.swagger.SwaggerResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/person.json")
@Api("/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource extends SwaggerResource {

    private final PeopleDAO peopleDAO;

    public PersonResource(PeopleDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @GET
    @ApiOperation(value = "Get Person", responseClass = "com.example.helloworld.core.Person")
    @Path("/{personId}")
    public Person getPerson(@ApiParam(required = true) @PathParam("personId") LongParam personId) {
        return peopleDAO.findById(personId.get());
    }

}
