package io.dropwizard.hibernate;

import jakarta.ws.rs.ext.Provider;
import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.hibernate.SessionFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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

    private final ConcurrentMap<ResourceMethod, Collection<UnitOfWork>> methodMap = new ConcurrentHashMap<>();
    private final Map<String, SessionFactory> sessionFactories = new HashMap<>();

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
    public UnitOfWorkApplicationListener(String name, SessionFactory sessionFactory) {
        registerSessionFactory(name, sessionFactory);
    }

    /**
     * Register a session factory with the given name.
     *
     * @param name a name of a Hibernate bundle
     * @param sessionFactory a {@link SessionFactory}
     */
    public void registerSessionFactory(String name, SessionFactory sessionFactory) {
        sessionFactories.put(name, sessionFactory);
    }

    private static class UnitOfWorkEventListener implements RequestEventListener {
        private final ConcurrentMap<ResourceMethod, Collection<UnitOfWork>> methodMap;
        private final ConcurrentMap<String, UnitOfWorkAspect> unitOfWorkAspects = new ConcurrentHashMap<>();
        private final Map<String, SessionFactory> sessionFactories;

        UnitOfWorkEventListener(ConcurrentMap<ResourceMethod, Collection<UnitOfWork>> methodMap,
                                Map<String, SessionFactory> sessionFactories) {
            this.methodMap = methodMap;
            this.sessionFactories = sessionFactories;
        }

        @Override
        public void onEvent(RequestEvent event) {
            final RequestEvent.Type eventType = event.getType();
            if (eventType == RequestEvent.Type.RESOURCE_METHOD_START) {
                try {
                    methodMap
                        .computeIfAbsent(event.getUriInfo().getMatchedResourceMethod(), UnitOfWorkEventListener::registerUnitOfWorkAnnotations)
                        .forEach(unitOfWork ->
                            unitOfWorkAspects
                                .computeIfAbsent(unitOfWork.value(), hibernateName -> new UnitOfWorkAspect(sessionFactories))
                                .beforeStart(unitOfWork)
                    );
                } catch (IllegalArgumentException e) {
                    throw e;
                } catch (Exception e) {
                    throw new MappableException(e);
                }
            } else if (eventType == RequestEvent.Type.RESP_FILTERS_START) {
                try {
                    unitOfWorkAspects
                        .values()
                        .forEach(UnitOfWorkAspect::afterEnd);
                } catch (Exception e) {
                    throw new MappableException(e);
                }
            } else if (eventType == RequestEvent.Type.ON_EXCEPTION) {
                unitOfWorkAspects
                        .values()
                        .forEach(UnitOfWorkAspect::onError);
            } else if (eventType == RequestEvent.Type.FINISHED) {
                unitOfWorkAspects
                        .values()
                        .forEach(UnitOfWorkAspect::onFinish);
            }
        }

        private static Collection<UnitOfWork> registerUnitOfWorkAnnotations(ResourceMethod method) {
            Map<String, UnitOfWork> unitOfWorkMap = new HashMap<>();
            Arrays.stream(method.getInvocable().getHandlingMethod().getAnnotationsByType(UnitOfWork.class))
                .forEach(unitOfWork -> unitOfWorkMap.put(unitOfWork.value(), unitOfWork));
            Arrays.stream(method.getInvocable().getDefinitionMethod().getAnnotationsByType(UnitOfWork.class))
                .forEach(unitOfWork -> unitOfWorkMap.put(unitOfWork.value(), unitOfWork));
            return unitOfWorkMap.values();
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        // Nothing to do
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        return new UnitOfWorkEventListener(methodMap, sessionFactories);
    }

}
