package com.example.helloworld.cli;

import com.beust.jcommander.Parameters;
import com.example.helloworld.HelloWorldConfiguration;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Bootstrap;

@Parameters(commandNames = "setup",
            commandDescription = "Setup the database")
public class SetupDatabaseCommand extends ConfiguredCommand<HelloWorldConfiguration> {
    @Override
    protected void run(Bootstrap<HelloWorldConfiguration> bootstrap,
                       HelloWorldConfiguration configuration) throws Exception {
//        final Log log = Log.forClass(SetupDatabaseCommand.class);
//        final Environment environment = new Environment(configuration,
//                                                        ObjectMapperFactory.defaultInstance());
//        bootstrap.runWithBundles();
//        final DatabaseFactory factory = new DatabaseFactory(environment);
//        final Database db = factory.build(configuration.getDatabaseConfiguration(), "h2");
//        final PeopleDAO peopleDAO = db.onDemand(PeopleDAO.class);
//
//        log.info("creating tables.");
//        peopleDAO.createPeopleTable();
    }
}
