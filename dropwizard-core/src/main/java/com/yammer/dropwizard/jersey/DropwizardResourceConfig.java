package com.yammer.dropwizard.jersey;

import com.sun.jersey.api.core.ScanningResourceConfig;
import com.yammer.dropwizard.config.ValidatorConfiguration;
import com.yammer.dropwizard.jersey.caching.CacheControlledResourceMethodDispatchAdapter;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;

public class DropwizardResourceConfig extends ScanningResourceConfig {

    public DropwizardResourceConfig(boolean testOnly) {
        super();
        init(testOnly, null);
    }

    public DropwizardResourceConfig(boolean testOnly, ValidatorConfiguration validatorConfiguration) {
        super();
        init(testOnly, validatorConfiguration);
    }

    private void init (boolean testOnly, ValidatorConfiguration validatorConfiguration) {
        getFeatures().put(FEATURE_DISABLE_WADL, Boolean.TRUE);
        if (!testOnly) {
            // create a subclass to pin it to Throwable
            getSingletons().add(new LoggingExceptionMapper<Throwable>() {});
            getSingletons().add(new JsonProcessingExceptionMapper());

            configureValidator(validatorConfiguration);
        }
        getClasses().add(InstrumentedResourceMethodDispatchAdapter.class);
        getClasses().add(CacheControlledResourceMethodDispatchAdapter.class);
        getClasses().add(OptionalResourceMethodDispatchAdapter.class);
        getClasses().add(OptionalQueryParamInjectableProvider.class);
    }

    private void configureValidator(ValidatorConfiguration validatorConfiguration) {
        if (validatorConfiguration == null) {
            getSingletons().add(new InvalidEntityExceptionMapper());
        } else {
            getSingletons().add(validatorConfiguration.getInvalidEntityExceptionMapper());
        }
    }
}
