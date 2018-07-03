package io.dropwizard.hibernate;

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


/**
 * An application event listener that listens for Jersey application initialization to
 * be finished, then creates a map of resource method that have metrics annotations.
 *
 * Finally, it listens for method start events, and returns a {@link RequestEventListener}
 * that updates the relevant metric for suitably annotated methods when it gets the
 * request events indicating that the method is about to be invoked, or just got done
 * being invoked.
 */
@Provider
public class UnitOfWorkApplicationListener implements ApplicationEventListener {

    private ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap = new ConcurrentHashMap<>();
    private Map<String, ClusteredSessionFactory> clusteredSessionFactories = new HashMap<>();

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
     * @param clusteredSessionFactory a {@link ClusteredSessionFactory}
     */
    public UnitOfWorkApplicationListener(String name, ClusteredSessionFactory clusteredSessionFactory) {
        registerSessionFactory(name, clusteredSessionFactory);
    }

    /**
     * Register a session factory with the given name.
     *
     * @param name a name of a Hibernate bundle
     * @param clusteredSessionFactory a {@link ClusteredSessionFactory}
     */
    public void registerSessionFactory(String name, ClusteredSessionFactory clusteredSessionFactory) {
        clusteredSessionFactories.put(name, clusteredSessionFactory);
    }

    private static class UnitOfWorkEventListener implements RequestEventListener {
        private ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap;
        private final UnitOfWorkAspect unitOfWorkAspect;

        UnitOfWorkEventListener(ConcurrentMap<ResourceMethod, Optional<UnitOfWork>> methodMap,
                                Map<String, ClusteredSessionFactory> clusteredSessionFactories) {
            this.methodMap = methodMap;
            unitOfWorkAspect = new UnitOfWorkAspect(clusteredSessionFactories);
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
        return new UnitOfWorkEventListener(methodMap, clusteredSessionFactories);
    }
}
