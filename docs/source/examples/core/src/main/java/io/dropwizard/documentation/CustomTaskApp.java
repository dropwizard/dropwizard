package io.dropwizard.documentation;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.documentation.db.Database;

public class CustomTaskApp extends Application<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) {
        Database database = new Database();
        environment.admin().addTask(new TruncateDatabaseTask(database));
    }
}
