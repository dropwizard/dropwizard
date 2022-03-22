package io.dropwizard.documentation;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class AssetsApp extends Application<Configuration> {
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/"));

        bootstrap.addBundle(new AssetsBundle("/assets/css", "/css", null, "css"));
        bootstrap.addBundle(new AssetsBundle("/assets/js", "/js", null, "js"));
        bootstrap.addBundle(new AssetsBundle("/assets/fonts", "/fonts", null, "fonts"));
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
    }
}
