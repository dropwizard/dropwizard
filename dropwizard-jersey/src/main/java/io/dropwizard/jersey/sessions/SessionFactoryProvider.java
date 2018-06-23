package io.dropwizard.jersey.sessions;

import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractValueParamProvider;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractorProvider;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import java.util.function.Function;

@Singleton
public class SessionFactoryProvider extends AbstractValueParamProvider {

    private final InjectionManager im;

    @Inject
    public SessionFactoryProvider(final Provider<MultivaluedParameterExtractorProvider> extractorProvider, InjectionManager im) {
        super(extractorProvider, Parameter.Source.UNKNOWN);
        this.im = im;
    }

    @Nullable
    @Override
    protected Function<ContainerRequest, ?> createValueProvider(Parameter parameter) {
        final Class<?> classType = parameter.getRawType();

        final Session sessionAnnotation = parameter.getAnnotation(Session.class);
        if (sessionAnnotation == null) {
            return null;
        }

        if (classType.isAssignableFrom(HttpSession.class)) {
            return x -> im.createAndInitialize(HttpSessionFactory.class).provide(sessionAnnotation.doNotCreate());
        } else if (classType.isAssignableFrom(Flash.class)) {
            return x -> im.createAndInitialize(FlashFactory.class).provide(sessionAnnotation.doNotCreate());
        } else {
            return null;
        }
    }

    public static class Binder extends AbstractBinder {
        @Override
        protected void configure() {
            bind(SessionFactoryProvider.class).to(ValueParamProvider.class).in(Singleton.class);
        }
    }
}
