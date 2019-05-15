package io.dropwizard.jersey.optional;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverterProvider;

public class OptionalParamBinder extends AbstractBinder {
    @Override
    protected void configure() {
        // Param converter providers
        bind(OptionalParamConverterProvider.class).to(ParamConverterProvider.class).in(Singleton.class);
        bind(OptionalDoubleParamConverterProvider.class).to(ParamConverterProvider.class).in(Singleton.class);
        bind(OptionalIntParamConverterProvider.class).to(ParamConverterProvider.class).in(Singleton.class);
        bind(OptionalLongParamConverterProvider.class).to(ParamConverterProvider.class).in(Singleton.class);
    }
}
