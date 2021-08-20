package io.dropwizard.hibernate.dual;

import io.dropwizard.hibernate.UnitOfWork;

import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** Jersey server listener that wraps dual Hibernate session factories (writer & reader).
 * 
 * @since 2.1.13
 *
 */

@Provider
public class UnitOfWorkApplicationListener implements ApplicationEventListener {

    private ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap = new ConcurrentHashMap<>();
    private Map<String, DualSessionFactory> sessionFactories = new HashMap<>();

    public UnitOfWorkApplicationListener() {
    }

    /**
     * Construct an application event listener using the given name and session factory.
     *
     * <p/>
     * When using this constructor, the {@link UnitOfWorkApplicationListener}
     * should be added to a Jersey {@code ResourceConfig} as a singleton.
     *
     * @param name a name of a Hibernate bundle
     * @param sessionFactory a {@link SessionFactory}
     */
    public UnitOfWorkApplicationListener(String name, DualSessionFactory sessionFactory) {
        registerSessionFactory(name, sessionFactory);
    }

    /**
     * Register a session factory with the given name.
     *
     * @param name a name of a Hibernate bundle
     * @param sessionFactory a {@link SessionFactory}
     */
    public void registerSessionFactory(String name, DualSessionFactory sessionFactory) {
        sessionFactories.put(name, sessionFactory);
    }

    private static class UnitOfWorkEventListener implements RequestEventListener {
        private ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap;
        private final UnitOfWorkAspect unitOfWorkAspect;

        UnitOfWorkEventListener(ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap,
                                Map<String, DualSessionFactory> sessionFactories) {
            this.methodMap = methodMap;
            unitOfWorkAspect = new UnitOfWorkAspect(sessionFactories);
        }

        @Override
        public void onEvent(RequestEvent event) {
            final RequestEvent.Type eventType = event.getType();
            if (eventType == RequestEvent.Type.RESOURCE_METHOD_START) {
                Optional<UnitOfWork> unitOfWork = methodMap.computeIfAbsent(event.getUriInfo()
                        .getMatchedResourceMethod(), UnitOfWorkEventListener::registerUnitOfWorkAnnotations);
                unitOfWorkAspect.beforeStart(unitOfWork.orElse(null));
            } else if (eventType == RequestEvent.Type.RESP_FILTERS_START) {
                try {
                    unitOfWorkAspect.afterEnd();
                } catch (Exception e) {
                    throw new MappableException(e);
                }
            } else if (eventType == RequestEvent.Type.ON_EXCEPTION) {
                unitOfWorkAspect.onError();
            } else if (eventType == RequestEvent.Type.FINISHED) {
                unitOfWorkAspect.onFinish();
            }
        }

        private static Optional<UnitOfWork> registerUnitOfWorkAnnotations(ResourceMethod method) {
            UnitOfWork annotation = method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfWork.class);
            if (annotation == null) {
                annotation = method.getInvocable().getHandlingMethod().getAnnotation(UnitOfWork.class);
            }
            return Optional.ofNullable(annotation);
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        return new UnitOfWorkEventListener(methodMap, sessionFactories);
    }


}
