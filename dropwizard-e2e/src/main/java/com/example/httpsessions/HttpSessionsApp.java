package com.example.httpsessions;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.session.SessionHandler;

public class HttpSessionsApp extends Application<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.servlets().setSessionHandler(new SessionHandler());
        environment.jersey().register(new HttpSessionsResource());
    }
}
