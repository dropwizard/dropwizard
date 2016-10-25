package com.example.app1;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.optional.EmptyOptionalException;
import io.dropwizard.setup.Environment;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

public class App1 extends Application<Configuration> {
    @Override
    public void run(Configuration config, Environment env) throws Exception {
        // Ensure that we can override the default 404 response on an
        // empty optional and return a 204 instead
        env.jersey().register(new ExceptionMapper<EmptyOptionalException>() {
            @Override
            public Response toResponse(EmptyOptionalException exception) {
                return Response.noContent().build();
            }
        });

        env.jersey().register(new App1Resource());
    }
}
