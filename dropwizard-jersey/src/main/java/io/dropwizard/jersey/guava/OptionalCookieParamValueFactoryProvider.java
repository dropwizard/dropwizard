package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractor;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class OptionalCookieParamValueFactoryProvider extends AbstractValueFactoryProvider {

    @Inject
    protected OptionalCookieParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
        super(mpep, locator, Parameter.Source.COOKIE);
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        if (parameter.getRawType().equals(Optional.class)) {
            MultivaluedParameterExtractor extractor = get(unpack(parameter));
            return new OptionalCookieParamValueFactory(extractor);
        }
        return null;
    }

    private Parameter unpack(Parameter parameter) {
        ClassTypePair typePair = ReflectionHelper.getTypeArgumentAndClass(parameter.getType()).get(0);
        return Parameter.create(null, null, parameter.isEncoded(), typePair.rawClass(), typePair.type(), parameter.getAnnotations());
    }
}
