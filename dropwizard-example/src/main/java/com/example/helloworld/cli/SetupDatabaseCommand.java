package com.example.helloworld.cli;

import com.beust.jcommander.Parameters;
import com.example.helloworld.HelloWorldConfiguration;
import com.example.helloworld.db.PeopleDAO;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.Database;
import com.yammer.dropwizard.db.DatabaseFactory;
import com.yammer.dropwizard.logging.Log;

@Parameters(commandNames = "setup",
            commandDescription = "Setup the database")
public class SetupDatabaseCommand extends ConfiguredCommand<HelloWorldConfiguration> {

    @Override
    protected void run(Service<HelloWorldConfiguration> service,
                       HelloWorldConfiguration configuration) throws Exception {
        final Log log = Log.forClass(SetupDatabaseCommand.class);
        final Environment environment = new Environment(service, configuration);
//        service.initializeWithBundles(configuration, environment);
        final DatabaseFactory factory = new DatabaseFactory(environment);
        final Database db = factory.build(configuration.getDatabaseConfiguration(), "h2");
        final PeopleDAO peopleDAO = db.onDemand(PeopleDAO.class);

        log.info("creating tables.");
        peopleDAO.createPeopleTable();
    }
}
