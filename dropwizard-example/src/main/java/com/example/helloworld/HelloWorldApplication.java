package com.example.helloworld;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.assets.AssetsBundle;
import com.codahale.dropwizard.auth.basic.BasicAuthProvider;
import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.hibernate.HibernateBundle;
import com.codahale.dropwizard.migrations.MigrationsBundle;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.dropwizard.views.ViewBundle;
import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.cli.RenderCommand;
import com.example.helloworld.core.Person;
import com.example.helloworld.core.Template;
import com.example.helloworld.db.PersonDAO;
import com.example.helloworld.health.TemplateHealthCheck;
import com.example.helloworld.resources.*;

public class HelloWorldApplication extends Application<HelloWorldConfiguration> {
    public static void main(String[] args) throws Exception {
        new HelloWorldApplication().run(args);
    }

    private final HibernateBundle<HelloWorldConfiguration> hibernateBundle =
            new HibernateBundle<HelloWorldConfiguration>(Person.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    @Override
    public String getName() {
        return "hello-world";
    }

    @Override
    public void initialize(Bootstrap<HelloWorldConfiguration> bootstrap) {
        bootstrap.addCommand(new RenderCommand());
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new MigrationsBundle<HelloWorldConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(HelloWorldConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });
        bootstrap.addBundle(hibernateBundle);
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(HelloWorldConfiguration configuration,
                    Environment environment) throws ClassNotFoundException {
        final PersonDAO dao = new PersonDAO(hibernateBundle.getSessionFactory());
        final Template template = configuration.buildTemplate();

        environment.admin().addHealthCheck("template", new TemplateHealthCheck(template));

        environment.jersey().addProvider(new BasicAuthProvider<>(new ExampleAuthenticator(),
                                                                 "SUPER SECRET STUFF"));
        environment.jersey().addResource(new HelloWorldResource(template));
        environment.jersey().addResource(new ViewResource());
        environment.jersey().addResource(new ProtectedResource());
        environment.jersey().addResource(new PeopleResource(dao));
        environment.jersey().addResource(new PersonResource(dao));
    }
}
