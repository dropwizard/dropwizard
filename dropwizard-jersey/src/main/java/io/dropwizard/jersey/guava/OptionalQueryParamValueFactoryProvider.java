package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractor;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.QueryParam;

@Singleton
public class OptionalQueryParamValueFactoryProvider extends AbstractValueFactoryProvider {

    @Inject
    protected OptionalQueryParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
        super(mpep, locator, Parameter.Source.QUERY);
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        if (parameter.getRawType().equals(Optional.class)) {
            MultivaluedParameterExtractor extractor = get(unpack(parameter));
            return new OptionalQueryParamValueFactory(extractor, !parameter.isEncoded());
        }
        return null;
    }

    private Parameter unpack(Parameter parameter) {
        ClassTypePair typePair = ReflectionHelper.getTypeArgumentAndClass(parameter.getType()).get(0);
        return Parameter.create(null, null, parameter.isEncoded(), typePair.rawClass(), typePair.type(), parameter.getAnnotations());
    }

    @Singleton
    private static class OptionalInjectionResolver extends ParamInjectionResolver<QueryParam> {

        public OptionalInjectionResolver() {
            super(OptionalQueryParamValueFactoryProvider.class);
        }
    }

    public static class Binder extends AbstractBinder {

        @Override
        protected void configure() {
            bind(OptionalQueryParamValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            bind(OptionalInjectionResolver.class).to(new TypeLiteral<Object>(){}).in(Singleton.class);
        }
    }
}
