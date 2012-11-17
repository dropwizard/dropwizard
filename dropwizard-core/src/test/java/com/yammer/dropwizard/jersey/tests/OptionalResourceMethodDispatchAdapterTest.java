package com.yammer.dropwizard.jersey.tests;

import com.google.common.base.Optional;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class OptionalResourceMethodDispatchAdapterTest extends JerseyTest {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class ExampleResource {
        @GET
        public Optional<String> show(@QueryParam("id") String id) {
            return Optional.fromNullable(id);
        }
    }

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = new DropwizardResourceConfig(true);
        config.getSingletons().add(new ExampleResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void presentOptionalsReturnTheirValue() throws Exception {
        assertThat(client().resource("/test/")
                           .queryParam("id", "woo")
                           .get(String.class))
                .isEqualTo("woo");
    }

    @Test
    public void absentOptionalsThrowANotFound() throws Exception {
        try {
            client().resource("/test/").get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(404);
        }
    }
}
