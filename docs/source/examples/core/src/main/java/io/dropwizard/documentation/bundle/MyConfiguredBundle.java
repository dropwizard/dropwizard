package io.dropwizard.documentation.bundle;

import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class MyConfiguredBundle implements ConfiguredBundle<MyConfiguredBundleConfig> {
    @Override
    public void run(MyConfiguredBundleConfig applicationConfig, Environment environment) {
        applicationConfig.getBundleSpecificConfig();
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
    }
}

