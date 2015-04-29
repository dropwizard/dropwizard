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
import org.jvnet.hk2.annotations.Contract;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AuthValueFactoryProvider<T> extends AbstractValueFactoryProvider {
    private Class<?> clazz;
    /**
     * {@link Auth} annotation value factory provider injection constructor.
     *
     * @param mpep
     *            multivalued parameter extractor provider.
     * @param injector
     *            injector instance.
     */
    @Inject
    public AuthValueFactoryProvider(MultivaluedParameterExtractorProvider mpep, ServiceLocator injector) {
        super(mpep, injector, Parameter.Source.UNKNOWN);
        PrincipalClassProvider service = injector.getService(PrincipalClassProvider.class);
        clazz = service.getClazz();
    }

    /**
     * Return a factory for the provided parameter. We only expect objects of type {@link T} being annotated with
     * {@link Auth} annotation
     *
     * @param parameter
     *            Parameter that was annotated for being injected
     * @return {@link AuthValueFactory} if parameter matched type
     */
    @Override
    public AbstractContainerRequestValueFactory<?> createValueFactory(Parameter parameter) {
        Class<?> classType = parameter.getRawType();

        if (classType == null || (!classType.equals(clazz))) {
            return null;
        }

        return new AuthValueFactory<T>();
    }

    @Singleton
    static final class AuthInjectionResolver extends ParamInjectionResolver<Auth> {
        /**
         * Create new {@link Auth} annotation injection resolver.
         */
        public AuthInjectionResolver() {
            super(AuthValueFactoryProvider.class);
        }
    }

    @Contract
    public interface PrincipalClassProvider {
        Class<?> getClazz();
    }

    private static final class AuthValueFactory<T> extends AbstractContainerRequestValueFactory {
        /**
         * @return {@link T} stored on the request, or NULL if no object was found.
         */
        public T provide() {
            @SuppressWarnings("unchecked")
            T principal = (T) getContainerRequest().getSecurityContext().getUserPrincipal();
            if (principal == null) {
                throw new RuntimeException("Cannot inject Custom principal into unauthenticated request");
            }
            return principal;
        }
    }

    public static class Binder<T> extends AbstractBinder {
        private final Class<T> clazz;
        public Binder(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        protected void configure() {
            bind(new PrincipalClassProvider() {
                @Override
                public Class<?> getClazz() {
                    return clazz;
                }
            }).to(PrincipalClassProvider.class);
            bind(AuthValueFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            TypeLiteral<InjectionResolver<Auth>> typeLiteral
                    = new TypeLiteral<InjectionResolver<Auth>>() { };
            bind(AuthInjectionResolver.class).to(typeLiteral).in(Singleton.class);
        }
    }
}
