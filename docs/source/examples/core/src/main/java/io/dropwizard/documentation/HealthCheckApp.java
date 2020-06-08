package io.dropwizard.documentation;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.documentation.db.Database;
import io.dropwizard.documentation.db.DatabaseHealthCheck;
import io.dropwizard.setup.Environment;

public class HealthCheckApp extends Application<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) {
        Database database = new Database();
        environment.healthChecks().register("database", new DatabaseHealthCheck(database));
    }
}
