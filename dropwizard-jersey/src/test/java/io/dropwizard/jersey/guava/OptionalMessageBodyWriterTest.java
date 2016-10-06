package io.dropwizard.jersey.guava;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;

public class OptionalMessageBodyWriterTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return DropwizardResourceConfig.forTesting(new MetricRegistry())
                .register(OptionalReturnResource.class);
    }

    @Test
    public void presentOptionalsReturnTheirValue() throws Exception {
        assertThat(target("/optional-return/")
                .queryParam("id", "woo").request()
                .get(String.class))
                .isEqualTo("woo");
    }
    
    @Test
    public void absentOptionalsReturnANoContent() throws Exception {
        Response response = target("/optional-return/").request().get();
        assertThat(response.getStatusInfo().getStatusCode()).isEqualTo(Response.Status.NO_CONTENT.getStatusCode());
    }

    @Path("/optional-return/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class OptionalReturnResource {
        @GET
        public Optional<String> showWithQueryParam(@QueryParam("id") String id) {
            return Optional.fromNullable(id);
        }

        @POST
        public Optional<String> showWithFormParam(@FormParam("id") String id) {
            return Optional.fromNullable(id);
        }
    }
}
