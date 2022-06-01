package io.dropwizard.documentation.bundle;

import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;

// core: MyConfiguredBundle
public class MyConfiguredBundle implements ConfiguredBundle<MyConfiguredBundleConfig> {
    @Override
    public void run(MyConfiguredBundleConfig applicationConfig, Environment environment) {
        applicationConfig.getBundleSpecificConfig();
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {}
}
// core: MyConfiguredBundle
