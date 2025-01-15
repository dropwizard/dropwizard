package io.dropwizard.auth;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jspecify.annotations.Nullable;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import java.lang.reflect.ParameterizedType;
import java.security.Principal;
import java.util.Optional;
import java.util.function.Function;

/**
 * Value factory provider supporting {@link Principal} injection
 * by the {@link Auth} annotation.
 *
 * @param <T> the type of the principal
 */
@Singleton
public class AuthValueFactoryProvider<T extends Principal> extends AbstractValueParamProvider {

    /**
     * Class of the provided {@link Principal}
     */
    private final Class<T> principalClass;

    /**
     * {@link Principal} value factory provider injection constructor.
     *
     * @param mpep                   multivalued parameter extractor provider
     * @param principalClassProvider provider of the principal class
     */
    @Inject
    public AuthValueFactoryProvider(MultivaluedParameterExtractorProvider mpep,
                                    PrincipalClassProvider<T> principalClassProvider) {
        super(() -> mpep, org.glassfish.jersey.model.Parameter.Source.UNKNOWN);
        this.principalClass = principalClassProvider.clazz;
    }

    @Nullable
    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        if (!parameter.isAnnotationPresent(Auth.class)) {
            return null;
        } else if (principalClass.equals(parameter.getRawType())) {
            return request -> new PrincipalContainerRequestValueFactory(request).provide();
        } else {
            final boolean isOptionalPrincipal = parameter.getRawType() == Optional.class
                && ParameterizedType.class.isAssignableFrom(parameter.getType().getClass())
                && principalClass == ((ParameterizedType) parameter.getType()).getActualTypeArguments()[0];

            return isOptionalPrincipal ? request -> new OptionalPrincipalContainerRequestValueFactory(request).provide() : null;
        }
    }

    @Singleton
    static class PrincipalClassProvider<T extends Principal> {

        private final Class<T> clazz;

        PrincipalClassProvider(Class<T> clazz) {
            this.clazz = clazz;
        }
    }

    /**
     * Injection binder for {@link AuthValueFactoryProvider}.
     *
     * @param <T> the type of the principal
     */
    public static class Binder<T extends Principal> extends AbstractBinder {

        private final Class<T> principalClass;

        public Binder(Class<T> principalClass) {
            this.principalClass = principalClass;
        }

        @Override
        protected void configure() {
            bind(new PrincipalClassProvider<>(principalClass)).to(PrincipalClassProvider.class);
            bind(AuthValueFactoryProvider.class).to(ValueParamProvider.class).in(Singleton.class);
        }
    }
}
