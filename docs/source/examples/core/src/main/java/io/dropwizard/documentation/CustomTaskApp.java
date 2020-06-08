package io.dropwizard.documentation;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.documentation.db.Database;
import io.dropwizard.setup.Environment;

public class CustomTaskApp extends Application<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) {
        Database database = new Database();
        environment.admin().addTask(new TruncateDatabaseTask(database));
    }
}
