package com.example.helloworld.resoureces;

import com.codahale.dropwizard.testing.ResourceTest;
import com.example.helloworld.core.Saying;
import com.example.helloworld.core.Template;
import com.example.helloworld.resources.HelloWorldResource;
import org.junit.Test;

import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.codahale.dropwizard.testing.JsonHelpers.asJson;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;

public class HelloWorldResourceTest extends ResourceTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    private HelloWorldResource resource = new HelloWorldResource(new Template("Hello, %s!", "Stranger"));

    @Override
    protected void setUpResources() throws Exception {
        addResource(resource);
    }

    @Test
    public void sayHello() throws Exception {
        assertThat(client().resource("/hello-world").get(String.class))
                .isEqualTo(asJson(new Saying(1, "Hello, Stranger!")));
    }

    @Test
    public void receiveHello() throws Exception {
        Saying saying = new Saying(101l, "abc");
        client().resource("/hello-world").type(MediaType.APPLICATION_JSON_TYPE).post(asJson(saying));
        spy(resource).receiveHello(saying);

    }

    @Test(expected = ConstraintViolationException.class)
    public void receiveInvalidHello() throws Exception {
        client().resource("/hello-world").type(MediaType.APPLICATION_JSON_TYPE).post(asJson(new Saying(101l, "abcdefg")));
    }
}
