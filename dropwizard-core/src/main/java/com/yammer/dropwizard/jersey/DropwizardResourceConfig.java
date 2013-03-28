package com.yammer.dropwizard.jersey;

import com.sun.jersey.api.core.ScanningResourceConfig;
import com.yammer.dropwizard.jersey.caching.CacheControlledResourceMethodDispatchAdapter;
import com.yammer.dropwizard.validation.InvalidEntityException;
import com.yammer.metrics.jersey.InstrumentedResourceMethodDispatchAdapter;

import java.lang.reflect.ParameterizedType;

public class DropwizardResourceConfig extends ScanningResourceConfig {

    public DropwizardResourceConfig(boolean testOnly) {
        super();
        getFeatures().put(FEATURE_DISABLE_WADL, Boolean.TRUE);
        if (!testOnly) {
            // create a subclass to pin it to Throwable
            getSingletons().add(new LoggingExceptionMapper<Throwable>() {});
            getSingletons().add(new JsonProcessingExceptionMapper());
        }
        getClasses().add(InstrumentedResourceMethodDispatchAdapter.class);
        getClasses().add(CacheControlledResourceMethodDispatchAdapter.class);
        getClasses().add(OptionalResourceMethodDispatchAdapter.class);
        getClasses().add(OptionalQueryParamInjectableProvider.class);
    }

    @Override
    public void validate() {
        super.validate();
        // Use default mapper for InvalidEntityException if client did not set one
        if (!exceptionMapperHasBeenRegistered(InvalidEntityException.class)) {
            getClasses().add(InvalidEntityExceptionMapper.class);
        }
    }

    public boolean exceptionMapperHasBeenRegistered(Class<? extends Exception> exceptionClass) {
        for (Class c : getClasses()) {
            if (c.getGenericInterfaces().length == 1
                    && c.getGenericInterfaces()[0] instanceof ParameterizedType
                    && ((ParameterizedType) c.getGenericInterfaces()[0]).getActualTypeArguments().length == 1
                    && ((ParameterizedType) c.getGenericInterfaces()[0]).getActualTypeArguments()[0].equals(exceptionClass)) {
                return true;
            }
        }
        return false;
    }

}
