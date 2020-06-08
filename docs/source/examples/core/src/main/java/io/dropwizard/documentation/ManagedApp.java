package io.dropwizard.documentation;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.documentation.riak.RiakClient;
import io.dropwizard.documentation.riak.RiakClientManager;
import io.dropwizard.setup.Environment;

public class ManagedApp extends Application<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) {
        RiakClient client = new RiakClient();
        RiakClientManager riakClientManager = new RiakClientManager(client);
        environment.lifecycle().manage(riakClientManager);
    }
}
