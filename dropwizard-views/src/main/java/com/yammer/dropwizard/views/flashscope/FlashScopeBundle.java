package com.yammer.dropwizard.views.flashscope;

import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class FlashScopeBundle<T> implements ConfiguredBundle<T> {

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        FlashScopeConfig flashScopeConfig = getFlashScopeConfig(configuration);
        environment.getJerseyEnvironment().addProvider(
                new FlashScopeResourceMethodDispatchAdapter(flashScopeConfig));
        environment.getJerseyEnvironment().addProvider(new FlashScopeInjectableProvider());
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        // nothing
    }

    /**
     * Template method to override if you want to override any of the flash scope's cookie parameters.
     * @param configuration the service's configuration
     * @return the flash scope configuration
     */
    protected FlashScopeConfig getFlashScopeConfig(T configuration) {
        return new FlashScopeConfig();
    }
}
