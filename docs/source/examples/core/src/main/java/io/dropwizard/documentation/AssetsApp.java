package io.dropwizard.documentation;

import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

public class AssetsApp extends Application<Configuration> {
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        // core: AssetsApp#initialize->AssetsBundle
        bootstrap.addBundle(new AssetsBundle("/assets/", "/"));
        // core: AssetsApp#initialize->AssetsBundle

        // core: AssetsApp#initialize->AssetsBundle->subfolders
        bootstrap.addBundle(new AssetsBundle("/assets/css", "/css", null, "css"));
        bootstrap.addBundle(new AssetsBundle("/assets/js", "/js", null, "js"));
        bootstrap.addBundle(new AssetsBundle("/assets/fonts", "/fonts", null, "fonts"));
        // core: AssetsApp#initialize->AssetsBundle->subfolders
    }

    @Override
    public void run(Configuration configuration, Environment environment) {
    }
}
