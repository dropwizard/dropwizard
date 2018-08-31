package io.dropwizard.auth;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.ParameterizedType;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * Value factory provider supporting injection of a hierarchy of
 * {@link Principal} subclasses by the {@link Auth} annotation.
 *
 * @param <T> the type acting as the superclass from which injected
 *            principals inherit
 */
@Singleton
public class PolymorphicAuthValueFactoryProvider<T extends Principal> extends AbstractValueParamProvider {
    /**
     * Set of provided {@link Principal} subclasses.
     */
    protected final Set<Class<? extends T>> principalClassSet;


    /**
     * {@link Principal} value factory provider injection constructor.
     *
     * @param mpep                      multivalued parameter extractor provider
     * @param injector                  injector instance
     * @param principalClassSetProvider provider(s) of the principal class
     */
    @Inject
    public PolymorphicAuthValueFactoryProvider(
        MultivaluedParameterExtractorProvider mpep,
        ServiceLocator injector,
        PrincipalClassSetProvider<T> principalClassSetProvider
    ) {
        super(() -> mpep, Parameter.Source.UNKNOWN);
        this.principalClassSet = principalClassSetProvider.clazzSet;
    }

    @Nullable
    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        if (!parameter.isAnnotationPresent(Auth.class)) {
            return null;
        } else if (principalClassSet.contains(parameter.getRawType())) {
            return request -> new PrincipalContainerRequestValueFactory(request).provide();
        } else {
            final boolean isOptionalPrincipal = parameter.getRawType() == Optional.class
                && ParameterizedType.class.isAssignableFrom(parameter.getType().getClass())
                && principalClassSet.contains(((ParameterizedType) parameter.getType()).getActualTypeArguments()[0]);

            return isOptionalPrincipal ? request -> new OptionalPrincipalContainerRequestValueFactory(request).provide() : null;
        }
    }

    @Singleton
    protected static class PrincipalClassSetProvider<T extends Principal> {

        private final Set<Class<? extends T>> clazzSet;

        public PrincipalClassSetProvider(Set<Class<? extends T>> clazzSet) {
            this.clazzSet = clazzSet;
        }
    }

    /**
     * Injection binder for {@link PolymorphicAuthValueFactoryProvider}.
     *
     * @param <T> the type of the principal
     */
    public static class Binder<T extends Principal> extends AbstractBinder {

        private final Set<Class<? extends T>> principalClassSet;

        public Binder(Set<Class<? extends T>> principalClassSet) {
            this.principalClassSet = principalClassSet;
        }

        @Override
        protected void configure() {
            bind(new PrincipalClassSetProvider<>(principalClassSet)).to(PrincipalClassSetProvider.class);
            bind(PolymorphicAuthValueFactoryProvider.class).to(ValueParamProvider.class).in(Singleton.class);
        }
    }
}
