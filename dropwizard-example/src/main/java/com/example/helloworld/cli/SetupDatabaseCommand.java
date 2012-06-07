package com.example.helloworld.cli;

import com.example.helloworld.HelloWorldConfiguration;
import com.example.helloworld.db.PeopleDAO;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.Database;
import com.yammer.dropwizard.db.DatabaseFactory;
import com.yammer.dropwizard.logging.Log;
import org.apache.commons.cli.CommandLine;

public class SetupDatabaseCommand extends ConfiguredCommand<HelloWorldConfiguration> {

    public SetupDatabaseCommand() {
        super("setup", "Setup the database.");
    }

    @Override
    protected void run(AbstractService<HelloWorldConfiguration> service, HelloWorldConfiguration configuration, CommandLine params) throws Exception {

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
