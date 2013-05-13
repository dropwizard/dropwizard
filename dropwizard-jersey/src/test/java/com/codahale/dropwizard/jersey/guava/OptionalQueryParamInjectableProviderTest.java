package com.codahale.dropwizard.jersey.guava;

import com.codahale.dropwizard.logging.LoggingFactory;
import com.google.common.base.Optional;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;

public class OptionalQueryParamInjectableProviderTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
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
        final DefaultResourceConfig config = new DefaultResourceConfig();
        config.getClasses().add(OptionalQueryParamInjectableProvider.class);
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
