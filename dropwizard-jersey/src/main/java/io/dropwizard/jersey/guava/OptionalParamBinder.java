package io.dropwizard.jersey.guava;

import org.glassfish.jersey.internal.inject.AbstractBinder;

import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.ParamConverterProvider;

public class OptionalParamBinder extends AbstractBinder {
    @Override
    protected void configure() {
        // Param converter providers
        bind(OptionalParamConverterProvider.class).to(ParamConverterProvider.class).in(Singleton.class);
    }
}
