package io.dropwizard.documentation;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.documentation.riak.RiakClient;
import io.dropwizard.documentation.riak.RiakClientManager;

public class ManagedApp extends Application<Configuration> {
    @Override
    public void run(Configuration configuration, Environment environment) {
        RiakClient client = new RiakClient();
        RiakClientManager riakClientManager = new RiakClientManager(client);
        environment.lifecycle().manage(riakClientManager);
    }
}
