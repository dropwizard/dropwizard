package io.dropwizard.hibernate;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.ext.Provider;

import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.ApplicationEventListener;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;


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
public class UnitOfWorkApplicationListener implements
        ApplicationEventListener {

    private final SessionFactory sessionFactory;

    /**
     * Construct an application event listener using the given session factory.
     *
     * <p/>
     * When using this constructor, the {@link UnitOfWorkApplicationListener}
     * should be added to a Jersey {@code ResourceConfig} as a singleton.
     *
     * @param sessionFactory a {@link SessionFactory}
     */
    public UnitOfWorkApplicationListener (SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private static class UnitOfWorkEventListener implements RequestEventListener {
        private final Map<Method,UnitOfWork> methodMap;
        private final SessionFactory sessionFactory;
        private UnitOfWork unitOfWork;
        Session session;


        public UnitOfWorkEventListener (Map<Method,UnitOfWork> methodMap,
                                        SessionFactory sessionFactory) {
            this.methodMap = methodMap;
            this.sessionFactory = sessionFactory;
        }

        @Override
        public void onEvent(RequestEvent event) {
            if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_START) {
                this.unitOfWork = this.methodMap.get(event.getUriInfo()
                        .getMatchedResourceMethod().getInvocable().getDefinitionMethod());
                if (unitOfWork != null) {
                    this.session = this.sessionFactory.openSession();
                    try {
                        configureSession();
                        ManagedSessionContext.bind(this.session);
                        beginTransaction();
                    } catch (Throwable th) {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(this.sessionFactory);
                        throw th;
                    }
                }
            }
            else if (event.getType() == RequestEvent.Type.RESOURCE_METHOD_FINISHED) {
                if (this.session != null) {
                    try {
                        commitTransaction();
                    } catch (Exception e) {
                        rollbackTransaction();
                        this.<RuntimeException>rethrow(e);
                    }
                    finally {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(this.sessionFactory);
                    }
                }
            }
            else if (event.getType() == RequestEvent.Type.ON_EXCEPTION) {
                if (this.session != null) {
                    try {
                        rollbackTransaction();
                    }
                    finally {
                        this.session.close();
                        this.session = null;
                        ManagedSessionContext.unbind(this.sessionFactory);
                    }
                }
            }
        }

        private void beginTransaction() {
            if (this.unitOfWork.transactional()) {
                this.session.beginTransaction();
            }
        }

        private void configureSession() {
            this.session.setDefaultReadOnly(this.unitOfWork.readOnly());
            this.session.setCacheMode(this.unitOfWork.cacheMode());
            this.session.setFlushMode(this.unitOfWork.flushMode());
        }

        private void rollbackTransaction() {
            if (this.unitOfWork.transactional()) {
                final Transaction txn = this.session.getTransaction();
                if (txn != null && txn.isActive()) {
                    txn.rollback();
                }
            }
        }

        private void commitTransaction() {
            if (this.unitOfWork.transactional()) {
                final Transaction txn = this.session.getTransaction();
                if (txn != null && txn.isActive()) {
                    txn.commit();
                }
            }
        }

        @SuppressWarnings("unchecked")
        private <E extends Exception> void rethrow(Exception e) throws E {
            throw (E) e;
        }
    }

    private Map<Method,UnitOfWork> methodMap = new HashMap<>();

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.getType() == ApplicationEvent.Type.INITIALIZATION_APP_FINISHED)
        {
            for (Resource resource : event.getResourceModel().getResources())
            {
                for (ResourceMethod method : resource.getAllMethods())
                {
                    registerUnitOfWorkAnnotations (method);
                }

                for (Resource childResource : resource.getChildResources())
                {
                    for (ResourceMethod method : childResource.getAllMethods())
                    {
                        registerUnitOfWorkAnnotations (method);
                    }
                }
            }
        }
    }

    @Override
    public RequestEventListener onRequest(RequestEvent event) {
        RequestEventListener listener = new UnitOfWorkEventListener (this.methodMap,
                                                                     this.sessionFactory);

        return listener;
    }

    private void registerUnitOfWorkAnnotations (ResourceMethod method) {
        UnitOfWork annotation = method.getInvocable().getDefinitionMethod().getAnnotation(UnitOfWork.class);

        if (annotation != null)
        {
            this.methodMap.put(method.getInvocable().getDefinitionMethod(),
                               annotation);
        }

    }
}
