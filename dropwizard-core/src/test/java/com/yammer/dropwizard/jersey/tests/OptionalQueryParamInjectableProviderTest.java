package com.yammer.dropwizard.jersey.tests;

import com.google.common.base.Optional;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.fest.assertions.api.Assertions.assertThat;

public class OptionalQueryParamInjectableProviderTest extends JerseyTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class ExampleResource {
        @GET
        public String show(@QueryParam("id") Optional<Integer> id) {
            return id.or(-1).toString();
        }
    }

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = new DropwizardResourceConfig(true);
        config.getSingletons().add(new ExampleResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void injectsAnAbsentOptionalInsteadOfNull() throws Exception {
        assertThat(client().resource("/test/")
                           .get(String.class))
                .isEqualTo("-1");
    }

    @Test
    public void injectsAPresentOptionalInsteadOfValue() throws Exception {
        assertThat(client().resource("/test/")
                           .queryParam("id", "200")
                           .get(String.class))
                .isEqualTo("200");
    }
}
