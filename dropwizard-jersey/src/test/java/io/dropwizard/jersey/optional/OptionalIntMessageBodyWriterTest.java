package io.dropwizard.jersey.optional;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class OptionalIntMessageBodyWriterTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .register(OptionalIntReturnResource.class);
    }

    @Test
    public void presentOptionalsReturnTheirValue() throws Exception {
        assertThat(target("optional-return")
                .queryParam("id", "1").request()
                .get(Integer.class))
                .isEqualTo(1);
    }

    @Test
    public void presentOptionalsReturnTheirValueWithResponse() throws Exception {
        assertThat(target("optional-return/response-wrapped")
                .queryParam("id", "1").request()
                .get(Integer.class))
                .isEqualTo(1);
    }

    @Test
    public void absentOptionalsThrowANotFound() throws Exception {
        try {
            target("optional-return").request().get(Integer.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(404);
        }
    }

    @Path("optional-return")
    @Produces(MediaType.TEXT_PLAIN)
    public static class OptionalIntReturnResource {
        @GET
        public OptionalInt showWithQueryParam(@QueryParam("id") OptionalInt id) {
            return id;
        }

        @POST
        public OptionalInt showWithFormParam(@FormParam("id") OptionalInt id) {
            return id;
        }

        @Path("response-wrapped")
        @GET
        public Response showWithQueryParamResponse(@QueryParam("id") OptionalInt id) {
            return Response.ok(id).build();
        }
    }
}
