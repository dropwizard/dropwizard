package io.dropwizard.testing.app;

import io.dropwizard.jersey.PATCH;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class TestResource {

    public static final String DEFAULT_MESSAGE = "Default message";

    private final String message;

    public TestResource() {
      this(DEFAULT_MESSAGE);
    }

    public TestResource(String message) {
        this.message = message;
    }

    @Path("test")
    @GET
    public String test() {
        return message;
    }

    @Path("message")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DropwizardTestApplication.MessageView messageView() {
        return new DropwizardTestApplication.MessageView(message);
    }

    @Path("echoPatch")
    @PATCH
    public String echoPatch(String patchMessage) {
        return patchMessage;
    }
}
