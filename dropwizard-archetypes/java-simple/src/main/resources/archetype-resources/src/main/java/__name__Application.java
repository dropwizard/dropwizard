package ${package};

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import ${package}.health.${name}HealthCheck;
import ${package}.resources.${name}Resource;

public class ${name}Application extends Application<${name}Configuration> {

    private String applicationName;

    public static void main(final String[] args) throws Exception {
        new ${name}Application().run(args);
    }

    @Override
    public String getName() {
        return applicationName;
    }

    @Override
    public void initialize(final Bootstrap<${name}Configuration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final ${name}Configuration configuration, final Environment environment) {
        applicationName = configuration.getApplicationName();

        environment.healthChecks().register("${artifactId}", new ${name}HealthCheck());
        environment.jersey().register(new ${name}Resource(applicationName));
    }

}
