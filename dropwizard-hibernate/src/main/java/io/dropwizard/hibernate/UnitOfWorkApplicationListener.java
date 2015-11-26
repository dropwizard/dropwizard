package io.dropwizard.hibernate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.internal.process.MappableException;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import org.hibernate.SessionFactory;


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

    private Map<Method, UnitOfWork> methodMap = new HashMap<>();
    private Map<String, SessionFactory> sessionFactories = new HashMap<>();

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
        private final Map<Method, UnitOfWork> methodMap;
        private final UnitOfWorkAspect unitOfWorkAspect;

        UnitOfWorkEventListener(Map<Method, UnitOfWork> methodMap,
                                       Map<String, SessionFactory> sessionFactories) {
            this.methodMap = methodMap;
            unitOfWorkAspect = new UnitOfWorkAspect(sessionFactories);
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                UnitOfWork unitOfWork = methodMap.get(event.getUriInfo()
                        .getMatchedResourceMethod().getInvocable().getDefinitionMethod());
                unitOfWorkAspect.beforeStart(unitOfWork);
            } else if (event.getType() == RequestEvent.Type.RESP_FILTERS_START) {
                try {
                    unitOfWorkAspect.afterEnd();
                } catch (Exception e) {
                    throw new MappableException(e);
                }
            } else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                unitOfWorkAspect.onError();
            }
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED) {
            for (Resource resource : event.getResourceModel().getResources()) {
                for (ResourceMethod method : resource.getAllMethods()) {
                    registerUnitOfWorkAnnotations(method);
                }

                for (Resource childResource : resource.getChildResources()) {
                    for (ResourceMethod method : childResource.getAllMethods()) {
                        registerUnitOfWorkAnnotations(method);
                    }
                }
            }
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        return new UnitOfWorkEventListener(methodMap, sessionFactories);
    }

    private void registerUnitOfWorkAnnotations(ResourceMethod method) {
        UnitOfWork annotation = method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfWork.class);

        if (annotation == null) {
            annotation = method.getInvocable().getHandlingMethod().getAnnotation(UnitOfWork.class);
        }

        if (annotation != null) {
            this.methodMap.put(method.getInvocable().getDefinitionMethod(), annotation);
        }

    }
}
