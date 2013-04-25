package com.codahale.dropwizard.jersey;

import com.codahale.dropwizard.jersey.caching.CacheControlledResourceMethodDispatchAdapter;
import com.codahale.dropwizard.jersey.errors.LoggingExceptionMapper;
import com.codahale.dropwizard.jersey.guava.OptionalQueryParamInjectableProvider;
import com.codahale.dropwizard.jersey.guava.OptionalResourceMethodDispatchAdapter;
import com.codahale.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import com.codahale.dropwizard.jersey.validation.ConstraintViolationExceptionMapper;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;
import com.sun.jersey.api.core.ScanningResourceConfig;

public class DropwizardResourceConfig extends ScanningResourceConfig {
    public DropwizardResourceConfig(boolean testOnly, MetricRegistry metricRegistry) {
        super();
        getFeatures().put(FEATURE_DISABLE_WADL, Boolean.TRUE);
        if (!testOnly) {
            // create a subclass to pin it to Throwable
            getSingletons().add(new LoggingExceptionMapper<Throwable>() {});
            getSingletons().add(new ConstraintViolationExceptionMapper());
            getSingletons().add(new JsonProcessingExceptionMapper());
        }
        getSingletons().add(new InstrumentedResourceMethodDispatchAdapter(metricRegistry));
        getClasses().add(CacheControlledResourceMethodDispatchAdapter.class);
        getClasses().add(OptionalResourceMethodDispatchAdapter.class);
        getClasses().add(OptionalQueryParamInjectableProvider.class);
    }
}
