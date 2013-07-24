package com.codahale.dropwizard.server;

import javax.validation.Validator;

import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.codahale.dropwizard.jersey.setup.JerseyContainerHolder;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.jetty.setup.ServletEnvironment;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.servlet.ServletContainer;

public class ServerEnvironment extends Environment
{
    private final JerseyContainerHolder jerseyServletContainer;
    private final JerseyEnvironment jerseyEnvironment;

    private final MutableServletContextHandler servletContext;
    private final ServletEnvironment servletEnvironment;

    private final MutableServletContextHandler adminContext;
    private final AdminEnvironment adminEnvironment;
    
    public ServerEnvironment(String name, ObjectMapper objectMapper,
        Validator validator, MetricRegistry metricRegistry, ClassLoader classLoader)
    {
        super(name, objectMapper, validator, metricRegistry, classLoader);
        
        this.servletContext = new MutableServletContextHandler();
        servletContext.setClassLoader(classLoader);
        this.servletEnvironment = new ServletEnvironment(servletContext);

        this.adminContext = new MutableServletContextHandler();
        adminContext.setClassLoader(classLoader);
        this.adminEnvironment = new AdminEnvironment(adminContext, healthChecks());

        final DropwizardResourceConfig jerseyConfig = new DropwizardResourceConfig(metricRegistry);
        this.jerseyServletContainer = new JerseyContainerHolder(new ServletContainer(jerseyConfig));
        this.jerseyEnvironment = new JerseyEnvironment(jerseyServletContainer, jerseyConfig);
    }
    
    public JerseyEnvironment jersey() {
        return jerseyEnvironment;
    }

    public AdminEnvironment admin() {
        return adminEnvironment;
    }

    public ServletEnvironment servlets() {
        return servletEnvironment;
    }

    /*
    * Internal Accessors
    */

    MutableServletContextHandler getApplicationContext() {
        return servletContext;
    }

    ServletContainer getJerseyServletContainer() {
        return jerseyServletContainer.getContainer();
    }

    MutableServletContextHandler getAdminContext() {
        return adminContext;
    }

}
