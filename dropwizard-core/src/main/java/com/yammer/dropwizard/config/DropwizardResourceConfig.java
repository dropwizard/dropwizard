package com.yammer.dropwizard.config;

import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.yammer.dropwizard.jersey.LoggingExceptionMapper;
import com.yammer.dropwizard.jersey.caching.CacheControlledResourceMethodDispatchAdapter;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;

public class DropwizardResourceConfig extends DefaultResourceConfig {
    public DropwizardResourceConfig() {
        super();
        getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.TRUE);
        getSingletons().add(new LoggingExceptionMapper<Throwable>() { }); // create a subclass to pin it to Throwable
        getClasses().add(InstrumentedResourceMethodDispatchAdapter.class);
        getClasses().add(CacheControlledResourceMethodDispatchAdapter.class);
    }
}
