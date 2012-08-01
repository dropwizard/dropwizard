package com.yammer.dropwizard.jersey;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.api.core.ScanningResourceConfig;
import com.yammer.dropwizard.jersey.caching.CacheControlledResourceMethodDispatchAdapter;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;

public class DropwizardResourceConfig extends ScanningResourceConfig {
    public DropwizardResourceConfig(boolean testOnly) {
        super();
        getFeatures().put(ResourceConfig.FEATURE_DISABLE_WADL, Boolean.TRUE);
        if (!testOnly) {
            // create a subclass to pin it to Throwable
            getSingletons().add(new LoggingExceptionMapper<Throwable>() {});
            getSingletons().add(new InvalidEntityExceptionMapper());
            getSingletons().add(new JsonProcessingExceptionMapper());
        }
        getClasses().add(InstrumentedResourceMethodDispatchAdapter.class);
        getClasses().add(CacheControlledResourceMethodDispatchAdapter.class);
    }
}
