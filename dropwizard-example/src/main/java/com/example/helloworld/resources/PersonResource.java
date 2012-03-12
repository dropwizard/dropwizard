package com.example.helloworld.resources;

import com.example.helloworld.core.Person;
import com.example.helloworld.db.PeopleDAO;
import com.yammer.dropwizard.jersey.params.LongParam;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/person")
@Produces(MediaType.APPLICATION_JSON)
public class PersonResource {

    private final PeopleDAO peopleDAO;

    public PersonResource(PeopleDAO peopleDAO) {
        this.peopleDAO = peopleDAO;
    }

    @GET
    @Path("{personId}")
    public Person getPerson(@PathParam("personId") LongParam personId) {
        return peopleDAO.findById(personId.get());
    }

}
