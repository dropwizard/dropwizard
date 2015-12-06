package io.dropwizard.jersey.sessions;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.internal.inject.AbstractValueFactoryProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

@Singleton
public class SessionFactoryProvider extends AbstractValueFactoryProvider {

    @Inject
    public SessionFactoryProvider(final MultivaluedParameterExtractorProvider extractorProvider,
                                  final ServiceLocator injector) {
        super(extractorProvider, injector, Parameter.Source.UNKNOWN);
    }

    @Override
    protected Factory<?> createValueFactory(final Parameter parameter) {
        final Class<?> classType = parameter.getRawType();

        final Session sessionAnnotation = parameter.getAnnotation(Session.class);
        if (sessionAnnotation == null) {
            return null;
        }

        if (classType.isAssignableFrom(HttpSession.class)) {
            return new HttpSessionFactory(sessionAnnotation.doNotCreate());
        } else if (classType.isAssignableFrom(Flash.class)) {
            return new FlashFactory(sessionAnnotation.doNotCreate());
        } else {
            return null;
        }
    }

    public static class SessionInjectionResolver extends ParamInjectionResolver<Session> {
        public SessionInjectionResolver() {
            super(SessionFactoryProvider.class);
        }
    }

    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(SessionFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
            bind(SessionInjectionResolver.class).to(
                    new TypeLiteral<InjectionResolver<Session>>() {
                    }
            ).in(Singleton.class);
        }
    }
}

