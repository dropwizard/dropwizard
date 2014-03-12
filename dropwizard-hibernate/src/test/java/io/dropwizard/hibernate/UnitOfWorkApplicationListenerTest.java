package io.dropwizard.hibernate;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.model.ResourceModel;
import org.glassfish.jersey.server.monitoring.ApplicationEvent;
import org.glassfish.jersey.server.monitoring.RequestEvent;
import org.glassfish.jersey.server.monitoring.RequestEventListener;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest({Resource.class,ResourceMethod.class,Invocable.class})
@RunWith(PowerMockRunner.class)
@SuppressWarnings("HibernateResourceOpenedButNotSafelyClosed")
public class UnitOfWorkApplicationListenerTest {
    private final SessionFactory sessionFactory = mock(SessionFactory.class);
    private final ApplicationEvent appEvent = mock(ApplicationEvent.class);
    private final ResourceModel model = mock(ResourceModel.class);
    private final Resource resource = PowerMockito.mock(Resource.class);
    private final ResourceMethod method = PowerMockito.mock(ResourceMethod.class);
    private final Invocable invocable = PowerMockito.mock(Invocable.class);
    private final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);

    private final RequestEvent requestStartEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodStartEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodFinishEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodExceptionEvent = mock(RequestEvent.class);
    private final UnitOfWorkApplicationListener listener =
        new UnitOfWorkApplicationListener(sessionFactory);

    private final Session session = mock(Session.class);
    private final Transaction transaction = mock(Transaction.class);
    
    public static class MockResource
    {   
        @UnitOfWork(readOnly=false,cacheMode=CacheMode.NORMAL,transactional=true,flushMode=FlushMode.AUTO)
        public void methodWithDefaultAnnotation ()
        {
        }
        
        @UnitOfWork(readOnly=true,cacheMode=CacheMode.NORMAL,transactional=true,flushMode=FlushMode.AUTO)
        public void methodWithReadOnlyAnnotation ()
        {
        }
        
        @UnitOfWork(readOnly=false,cacheMode=CacheMode.IGNORE,transactional=true,flushMode=FlushMode.AUTO)
        public void methodWithCacheModeIgnoreAnnotation ()
        {
        }
        
        @UnitOfWork(readOnly=false,cacheMode=CacheMode.NORMAL,transactional=true,flushMode=FlushMode.ALWAYS)
        public void methodWithFlushModeAlwaysAnnotation ()
        {
        }
        
        @UnitOfWork(readOnly=false,cacheMode=CacheMode.NORMAL,transactional=false,flushMode=FlushMode.AUTO)
        public void methodWithTransactionalFalseAnnotation ()
        {
        }
    }

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        when(sessionFactory.openSession()).thenReturn(session);
        when(session.getSessionFactory()).thenReturn(sessionFactory);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.getTransaction()).thenReturn(transaction);

        when(transaction.isActive()).thenReturn(true);

        when(appEvent.getType()).thenReturn(ApplicationEvent.Type.INITIALIZATION_APP_FINISHED);
        when(appEvent.getResourceModel()).thenReturn(model);
        when(model.getResources()).thenReturn(Collections.singletonList(resource));
        when(resource.getAllMethods()).thenReturn(Collections.singletonList(method));
        when(method.getInvocable()).thenReturn(invocable);
        when(invocable.getDefinitionMethod()).thenReturn(MockResource.class.getMethod("methodWithDefaultAnnotation"));

        when(requestMethodStartEvent.getType()).thenReturn(RequestEvent.Type.RESOURCE_METHOD_START);
        when(requestMethodFinishEvent.getType()).thenReturn(RequestEvent.Type.RESOURCE_METHOD_FINISHED);
        when(requestMethodExceptionEvent.getType()).thenReturn(RequestEvent.Type.ON_EXCEPTION);
        when(requestMethodStartEvent.getUriInfo()).thenReturn(uriInfo);
        when(requestMethodFinishEvent.getUriInfo()).thenReturn(uriInfo);
        when(requestMethodExceptionEvent.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getMatchedResourceMethod()).thenReturn(method);
    }

    @Test
    public void opensAndClosesASession() throws Exception {
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);

        final InOrder inOrder = inOrder(sessionFactory, session);
        inOrder.verify(sessionFactory).openSession();
        inOrder.verify(session).close();
    }

    @Test
    public void bindsAndUnbindsTheSessionToTheManagedContext() throws Exception {
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                assertThat(ManagedSessionContext.hasBind(sessionFactory))
                        .isTrue();
                return null;
            }
        }).when(session).beginTransaction();

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);


        assertThat(ManagedSessionContext.hasBind(sessionFactory))
                .isFalse();
    }

    @Test
    public void configuresTheSessionsReadOnlyDefault() throws Exception {
        when(invocable.getDefinitionMethod()).thenReturn(MockResource.class.getMethod("methodWithReadOnlyAnnotation"));

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);

        verify(session).setDefaultReadOnly(true);
    }

    @Test
    public void configuresTheSessionsCacheMode() throws Exception {
        when(invocable.getDefinitionMethod()).thenReturn(MockResource.class.getMethod("methodWithCacheModeIgnoreAnnotation"));

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);

        verify(session).setCacheMode(CacheMode.IGNORE);
    }

    @Test
    public void configuresTheSessionsFlushMode() throws Exception {
        when(invocable.getDefinitionMethod()).thenReturn(MockResource.class.getMethod("methodWithFlushModeAlwaysAnnotation"));

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);

        verify(session).setFlushMode(FlushMode.ALWAYS);
    }

    @Test
    public void doesNotBeginATransactionIfNotTransactional() throws Exception {
        when(invocable.getDefinitionMethod()).thenReturn(MockResource.class.getMethod("methodWithTransactionalFalseAnnotation"));
        when(session.getTransaction()).thenReturn(null);

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);

        verify(session, never()).beginTransaction();
        verifyZeroInteractions(transaction);
    }

    @Test
    public void beginsAndCommitsATransactionIfTransactional() throws Exception {
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);

        final InOrder inOrder = inOrder(session, transaction);
        inOrder.verify(session).beginTransaction();
        inOrder.verify(transaction).commit();
        inOrder.verify(session).close();
    }

    @Test
    public void rollsBackTheTransactionOnException() throws Exception {
        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodExceptionEvent);

        final InOrder inOrder = inOrder(session, transaction);
        inOrder.verify(session).beginTransaction();
        inOrder.verify(transaction).rollback();
        inOrder.verify(session).close();
    }

    @Test
    public void doesNotCommitAnInactiveTransaction() throws Exception {
        when(transaction.isActive()).thenReturn(false);

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);

        verify(transaction, never()).commit();
    }

    @Test
    public void doesNotCommitANullTransaction() throws Exception {
        when(session.getTransaction()).thenReturn(null);

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodFinishEvent);

        verify(transaction, never()).commit();
    }

    @Test
    public void doesNotRollbackAnInactiveTransaction() throws Exception {
        when(transaction.isActive()).thenReturn(false);

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodExceptionEvent);

        verify(transaction, never()).rollback();
    }

    @Test
    public void doesNotRollbackANullTransaction() throws Exception {
        when(session.getTransaction()).thenReturn(null);

        listener.onEvent(appEvent);
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(requestMethodExceptionEvent);

        verify(transaction, never()).rollback();
    }
}
