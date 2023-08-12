package com.example.httpsessions;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import org.eclipse.jetty.ee10.servlet.SessionHandler;

public class HttpSessionsApp extends Application<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.servlets().setSessionHandler(new SessionHandler());
        environment.jersey().register(new HttpSessionsResource());
    }
}
