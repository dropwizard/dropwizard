package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.inject.Providers;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.glassfish.jersey.internal.util.collection.ClassTypePair;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

@Singleton
public class OptionalParamConverterProvider implements ParamConverterProvider {
    private final InjectionManager manager;

    @Inject
    public OptionalParamConverterProvider(final InjectionManager manager) {
        this.manager = manager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType, final Annotation[] annotations) {
        if (Optional.class.equals(rawType)) {
            final List<ClassTypePair> ctps = ReflectionHelper.getTypeArgumentAndClass(genericType);
            final ClassTypePair ctp = (ctps.size() == 1) ? ctps.get(0) : null;

            if (ctp == null || ctp.rawClass() == String.class) {
                return new ParamConverter<T>() {
                    @Override
                    public T fromString(final String value) {
                        return rawType.cast(Optional.fromNullable(value));
                    }

                    @Override
                    public String toString(final T value) {
                        return value.toString();
                    }
                };
            }

            for (ParamConverterProvider provider : Providers.getProviders(manager, ParamConverterProvider.class)) {
                final ParamConverter<?> converter = provider.getConverter(ctp.rawClass(), ctp.type(), annotations);
                if (converter != null) {
                    return new ParamConverter<T>() {
                        @Override
                        public T fromString(final String value) {
                            final Object convertedValue = value == null ? null : converter.fromString(value);
                            return rawType.cast(Optional.fromNullable(convertedValue));
                        }

                        @Override
                        public String toString(final T value) {
                            return value.toString();
                        }
                    };
                }
            }
        }

        return null;
    }
}
