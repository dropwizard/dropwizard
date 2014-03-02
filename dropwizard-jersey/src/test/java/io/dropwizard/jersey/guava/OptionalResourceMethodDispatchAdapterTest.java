package io.dropwizard.jersey.guava;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class OptionalResourceMethodDispatchAdapterTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected Application configure() {
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        rc = rc.packages("io.dropwizard.jersey.guava");
        return rc;
    }

    @Test
    public void presentOptionalsReturnTheirValue() throws Exception {
        assertThat(target("/optional-return/")
                .queryParam("id", "woo").request()
                .get(String.class))
                .isEqualTo("woo");
    }

    @Test
    public void absentOptionalsThrowANotFound() throws Exception {
        try {
            target("/optional-return/").request().get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(404);
        }
    }
}
