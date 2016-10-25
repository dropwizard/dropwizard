package com.example.app1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.OptionalInt;

@Path("/")
public class App1Resource {
    @GET
    @Path("empty-optional")
    public OptionalInt emptyOptional() {
        return OptionalInt.empty();
    }
}
