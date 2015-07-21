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

import javax.inject.Inject;
import javax.inject.Singleton;
import java.security.Principal;

/**
 * Value factory provider supporting {@link Principal} injection
 * by the {@link Auth} annotation.
 *
 * @param <T> the type of the principal
 */
@Singleton
public class AuthValueFactoryProvider<T extends Principal> extends AbstractValueFactoryProvider {

    /**
     * Class of the provided {@link Principal}
     */
    private final Class<T> principalClass;

    /**
     * {@link Principal} value factory provider injection constructor.
     *
     * @param mpep                   multivalued parameter extractor provider
     * @param injector               injector instance
     * @param principalClassProvider provider of the principal class
     */
    @Inject
    public AuthValueFactoryProvider(MultivaluedParameterExtractorProvider mpep,
                                    ServiceLocator injector, PrincipalClassProvider<T> principalClassProvider) {
        super(mpep, injector, Parameter.Source.UNKNOWN);
        this.principalClass = principalClassProvider.clazz;
    }

    /**
     * Return a factory for the provided parameter. We only expect objects of
     * the type {@link T} being annotated with {@link Auth} annotation.
     *
     * @param parameter parameter that was annotated for being injected
     * @return the factory if parameter matched type
     */
    @Override
    public AbstractContainerRequestValueFactory<?> createValueFactory(Parameter parameter) {
        if (!principalClass.equals(parameter.getRawType())) {
            return null;
        }
        return new AbstractContainerRequestValueFactory<Principal>() {

            /**
             * @return {@link Principal} stored on the request, or {@code null} if no object was found.
             */
            public Principal provide() {
                final Principal principal = getContainerRequest().getSecurityContext().getUserPrincipal();
                if (principal == null) {
                    throw new IllegalStateException("Cannot inject a custom principal into unauthenticated request");
                }
                return principal;
            }
        };
    }

    @Singleton
    private static class AuthInjectionResolver extends ParamInjectionResolver<Auth> {

        /**
         * Create new {@link Auth} annotation injection resolver.
         */
        public AuthInjectionResolver() {
            super(AuthValueFactoryProvider.class);
        }
    }

    @Singleton
    private static class PrincipalClassProvider<T extends Principal> {

        private final Class<T> clazz;

        public PrincipalClassProvider(Class<T> clazz) {
            this.clazz = clazz;
        }
    }

    /**
     * Injection binder for {@link AuthValueFactoryProvider} and {@link AuthInjectionResolver}.
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
            bind(AuthValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            bind(AuthInjectionResolver.class).to(new TypeLiteral<InjectionResolver<Auth>>() {
            }).in(Singleton.class);
        }
    }
}
