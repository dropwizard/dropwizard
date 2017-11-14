package io.dropwizard.auth;

import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.ParameterizedType;
import java.security.Principal;
import java.util.Optional;
import java.util.Set;

/**
 * Value factory provider supporting injection of a hierarchy of
 * {@link Principal} subclasses by the {@link Auth} annotation.
 *
 * @param <T> the type acting as the superclass from which injected
 *            principals inherit
 */
@Singleton
public class PolymorphicAuthValueFactoryProvider<T extends Principal> extends AbstractValueFactoryProvider {
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
        super(mpep, injector, Parameter.Source.UNKNOWN);
        this.principalClassSet = principalClassSetProvider.clazzSet;
    }

    /**
     * Return a factory for the provided parameter. We only expect objects of
     * the type {@link T} being annotated with {@link Auth} annotation.
     *
     * @param parameter parameter that was annotated for being injected
     * @return the factory if annotated parameter matched type
     */
    @Override
    @Nullable
    public AbstractContainerRequestValueFactory<?> createValueFactory(Parameter parameter) {
        if (!parameter.isAnnotationPresent(Auth.class)) {
            return null;
        } else if (principalClassSet.contains(parameter.getRawType())) {
            return new PrincipalContainerRequestValueFactory();
        } else {
            final boolean isOptionalPrincipal = parameter.getRawType() == Optional.class
                && ParameterizedType.class.isAssignableFrom(parameter.getType().getClass())
                && principalClassSet.contains(((ParameterizedType) parameter.getType()).getActualTypeArguments()[0]);

            return isOptionalPrincipal ? new OptionalPrincipalContainerRequestValueFactory() : null;
        }
    }

    @Singleton
    static class AuthInjectionResolver extends ParamInjectionResolver<Auth> {

        /**
         * Create new {@link Auth} annotation injection resolver.
         */
        public AuthInjectionResolver() {
            super(PolymorphicAuthValueFactoryProvider.class);
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
     * Injection binder for {@link PolymorphicAuthValueFactoryProvider} and
     * {@link AuthInjectionResolver}.
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
            bind(PolymorphicAuthValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            bind(AuthInjectionResolver.class).to(new TypeLiteral<InjectionResolver<Auth>>() {
            }).in(Singleton.class);
        }
    }
}
