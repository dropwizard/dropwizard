package com.example.app1;

import com.github.mustachejava.MustacheNotFoundException;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.optional.EmptyOptionalNoContentExceptionMapper;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Throwables;
import io.dropwizard.views.ViewBundle;
import org.eclipse.jetty.io.EofException;
import org.glassfish.jersey.spi.ExtendedExceptionMapper;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class App1 extends Application<Configuration> {
    public volatile boolean wasEofExceptionHit = false;

    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new ViewBundle<>());
    }

    @Override
    public void run(Configuration config, Environment env) throws Exception {
        // Ensure that we can override the default 404 response on an
        // empty optional and return a 204 instead.
        env.jersey().register(new EmptyOptionalNoContentExceptionMapper());

        // This exception mapper ensures that we handle Jetty's EofException
        // the way we want to (we override the default simply to add instrumentation)
        env.jersey().register(new ExceptionMapper<EofException>() {
            @Override
            public Response toResponse(EofException exception) {
                wasEofExceptionHit = true;
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        });


        // Ensure that we can override the 503 response of a view that refers to
        // a missing Mustache template and return a 404 instead
        env.jersey().register(new ExtendedExceptionMapper<WebApplicationException>() {
            @Override
            public Response toResponse(WebApplicationException exception) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            @Override
            public boolean isMappable(WebApplicationException e) {
                return Throwables.getRootCause(e).getClass() == MustacheNotFoundException.class;
            }
        });

        env.jersey().register(new App1Resource());
        env.jersey().register(new CustomJsonProvider(env.getObjectMapper()));
        env.jersey().register(new CustomClassBodyWriter());
    }
}
