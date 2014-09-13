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

import static com.google.common.base.Strings.isNullOrEmpty;

@Singleton
public class OptionalFormParamValueFactoryProvider extends AbstractValueFactoryProvider {

    @Inject
    protected OptionalFormParamValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator locator) {
        super(mpep, locator, Parameter.Source.FORM);
    }

    @Override
    protected Factory<?> createValueFactory(Parameter parameter) {
        if (parameter.getRawType().equals(Optional.class)) {
            if (isNullOrEmpty(parameter.getSourceName())) {
                return null;
            }

            final MultivaluedParameterExtractor extractor = get(unpack(parameter));
            if (extractor == null) {
                return null;
            }

            return new OptionalFormParamValueFactory(extractor, !parameter.isEncoded());
        }

        return null;
    }

    private Parameter unpack(Parameter parameter) {
        ClassTypePair typePair = ReflectionHelper.getTypeArgumentAndClass(parameter.getType()).get(0);
        return Parameter.create(null, null, parameter.isEncoded(), typePair.rawClass(), typePair.type(), parameter.getAnnotations());
    }
}
