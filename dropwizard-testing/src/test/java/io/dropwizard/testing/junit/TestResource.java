package io.dropwizard.testing.junit;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
}
