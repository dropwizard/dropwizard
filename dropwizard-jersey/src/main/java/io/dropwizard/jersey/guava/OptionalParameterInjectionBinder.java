package io.dropwizard.jersey.guava;

import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Singleton;
import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;

public class OptionalParameterInjectionBinder extends AbstractBinder {

    @Override
    protected void configure() {
        bind(OptionalQueryParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(OptionalFormParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(OptionalCookieParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
        bind(OptionalHeaderParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);

        bind(OptionalQueryParamInjectionResolver.class).to(new TypeLiteral<Object>() {
        }).in(Singleton.class);
        bind(OptionalFormParamInjectionResolver.class).to(new TypeLiteral<Object>() {
        }).in(Singleton.class);
        bind(OptionalCookieParamInjectionResolver.class).to(new TypeLiteral<Object>() {
        }).in(Singleton.class);
        bind(OptionalHeaderParamInjectionResolver.class).to(new TypeLiteral<Object>() {
        }).in(Singleton.class);
    }

    @Singleton
    private static class OptionalQueryParamInjectionResolver extends ParamInjectionResolver<QueryParam> {

        public OptionalQueryParamInjectionResolver() {
            super(OptionalQueryParamValueFactoryProvider.class);
        }
    }

    @Singleton
    private static class OptionalFormParamInjectionResolver extends ParamInjectionResolver<FormParam> {
        public OptionalFormParamInjectionResolver() {
            super(OptionalFormParamValueFactoryProvider.class);
        }
    }

    @Singleton
    private static class OptionalCookieParamInjectionResolver extends ParamInjectionResolver<CookieParam> {
        public OptionalCookieParamInjectionResolver() {
            super(OptionalCookieParamValueFactoryProvider.class);
        }
    }

    @Singleton
    private static class OptionalHeaderParamInjectionResolver extends ParamInjectionResolver<HeaderParam> {
        public OptionalHeaderParamInjectionResolver() {
            super(OptionalHeaderParamValueFactoryProvider.class);
        }
    }
}
