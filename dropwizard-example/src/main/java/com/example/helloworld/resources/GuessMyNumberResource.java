package com.example.helloworld.resources;

import com.example.helloworld.views.GuessMyNumberView;
import com.yammer.dropwizard.views.flashscope.FlashIn;
import com.yammer.dropwizard.views.flashscope.FlashOut;
import com.yammer.dropwizard.views.flashscope.FlashScope;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.HttpHeaders.LOCATION;

@Path("/guess")
public class GuessMyNumberResource {

    @Path("form")
    @GET
    public GuessMyNumberView showGuessForm(@FlashScope FlashIn flashIn) {
        String message = flashIn.get("message");
        return new GuessMyNumberView(message != null ? message : "");
    }

    @Path("number")
    @POST
    public Response processGuess(@FlashScope FlashOut flashOut, @FormParam("number") String number) {
        if (number.equals("4")) {
            flashOut.put("message", "Correct!");
        } else {
            flashOut.put("message", "Wrong!");
        }

        return Response.status(302).header(LOCATION, "/guess/form").build();
    }

}
