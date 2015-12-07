package ${package}.resources;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ${name}Resource {

    private String applicationName;

    public ${name}Resource(String applicationName) {
        this.applicationName = applicationName;
    }

    @GET
    @Timed
    public String welcomeMessage() {
        return "You are now prepared to implement your dropwizard " + applicationName + " application. Have fun :-)";
    }
}
