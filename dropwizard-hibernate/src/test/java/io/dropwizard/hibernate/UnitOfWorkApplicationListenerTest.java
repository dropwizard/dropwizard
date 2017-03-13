package io.dropwizard.hibernate;

import org.glassfish.jersey.server.ExtendedUriInfo;
import org.glassfish.jersey.server.model.Resource;
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
import org.mockito.InOrder;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hibernate.resource.transaction.spi.TransactionStatus.ACTIVE;
import static org.hibernate.resource.transaction.spi.TransactionStatus.NOT_ACTIVE;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("HibernateResourceOpenedButNotSafelyClosed")
public class UnitOfWorkApplicationListenerTest {
    private final SessionFactory sessionFactory = mock(SessionFactory.class);
    private final SessionFactory analyticsSessionFactory = mock(SessionFactory.class);
    private final UnitOfWorkApplicationListener listener = new UnitOfWorkApplicationListener();
    private final ExtendedUriInfo uriInfo = mock(ExtendedUriInfo.class);

    private final RequestEvent requestStartEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodStartEvent = mock(RequestEvent.class);
    private final RequestEvent responseFiltersStartEvent = mock(RequestEvent.class);
    private final RequestEvent responseFinishedEvent = mock(RequestEvent.class);
    private final RequestEvent requestMethodExceptionEvent = mock(RequestEvent.class);
    private final Session session = mock(Session.class);
    private final Session analyticsSession = mock(Session.class);
    private final Transaction transaction = mock(Transaction.class);
    private final Transaction analyticsTransaction = mock(Transaction.class);

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        listener.registerSessionFactory(HibernateBundle.DEFAULT_NAME, sessionFactory);
        listener.registerSessionFactory("analytics", analyticsSessionFactory);

        when(sessionFactory.openSession()).thenReturn(session);
        when(session.getSessionFactory()).thenReturn(sessionFactory);
        when(session.beginTransaction()).thenReturn(transaction);
        when(session.getTransaction()).thenReturn(transaction);
        when(transaction.getStatus()).thenReturn(ACTIVE);

        when(analyticsSessionFactory.openSession()).thenReturn(analyticsSession);
        when(analyticsSession.getSessionFactory()).thenReturn(analyticsSessionFactory);
        when(analyticsSession.beginTransaction()).thenReturn(analyticsTransaction);
        when(analyticsSession.getTransaction()).thenReturn(analyticsTransaction);
        when(analyticsTransaction.getStatus()).thenReturn(ACTIVE);

        when(requestMethodStartEvent.getType()).thenReturn(RequestEvent.Type.RESOURCE_METHOD_START);
        when(responseFinishedEvent.getType()).thenReturn(RequestEvent.Type.FINISHED);
        when(requestMethodExceptionEvent.getType()).thenReturn(RequestEvent.Type.ON_EXCEPTION);
        when(responseFiltersStartEvent.getType()).thenReturn(RequestEvent.Type.RESP_FILTERS_START);
        when(requestMethodStartEvent.getUriInfo()).thenReturn(uriInfo);
        when(responseFinishedEvent.getUriInfo()).thenReturn(uriInfo);
        when(requestMethodExceptionEvent.getUriInfo()).thenReturn(uriInfo);

        prepareResourceMethod("methodWithDefaultAnnotation");
    }

    @Test
    public void opensAndClosesASession() throws Exception {
        execute();

        final InOrder inOrder = inOrder(sessionFactory, session);
        inOrder.verify(sessionFactory).openSession();
        inOrder.verify(session).close();
    }

    @Test
    public void bindsAndUnbindsTheSessionToTheManagedContext() throws Exception {
        doAnswer(invocation -> {
            assertThat(ManagedSessionContext.hasBind(sessionFactory))
                .isTrue();
            return null;
        }).when(session).beginTransaction();

        execute();

        assertThat(ManagedSessionContext.hasBind(sessionFactory)).isFalse();
    }

    @Test
    public void configuresTheSessionsReadOnlyDefault() throws Exception {
        prepareResourceMethod("methodWithReadOnlyAnnotation");

        execute();

        verify(session).setDefaultReadOnly(true);
    }

    @Test
    public void configuresTheSessionsCacheMode() throws Exception {
        prepareResourceMethod("methodWithCacheModeIgnoreAnnotation");

        execute();

        verify(session).setCacheMode(CacheMode.IGNORE);
    }

    @Test
    public void configuresTheSessionsFlushMode() throws Exception {
        prepareResourceMethod("methodWithFlushModeAlwaysAnnotation");

        execute();

        verify(session).setHibernateFlushMode(FlushMode.ALWAYS);
    }

    @Test
    public void doesNotBeginATransactionIfNotTransactional() throws Exception {
        final String resourceMethodName = "methodWithTransactionalFalseAnnotation";
        prepareResourceMethod(resourceMethodName);

        when(session.getTransaction()).thenReturn(null);

        execute();

        verify(session, never()).beginTransaction();
        verifyZeroInteractions(transaction);
    }

    @Test
    public void detectsAnnotationOnHandlingMethod() throws NoSuchMethodException {
        final String resourceMethodName = "handlingMethodAnnotated";
        prepareResourceMethod(resourceMethodName);

        execute();

        verify(session).setDefaultReadOnly(true);
    }

    @Test
    public void detectsAnnotationOnDefinitionMethod() throws NoSuchMethodException {
        final String resourceMethodName = "definitionMethodAnnotated";
        prepareResourceMethod(resourceMethodName);

        execute();

        verify(session).setDefaultReadOnly(true);
    }

    @Test
    public void annotationOnDefinitionMethodOverridesHandlingMethod() throws NoSuchMethodException {
        final String resourceMethodName = "bothMethodsAnnotated";
        prepareResourceMethod(resourceMethodName);

        execute();

        verify(session).setDefaultReadOnly(true);
    }

    @Test
    public void beginsAndCommitsATransactionIfTransactional() throws Exception {
        execute();

        final InOrder inOrder = inOrder(session, transaction);
        inOrder.verify(session).beginTransaction();
        inOrder.verify(transaction).commit();
        inOrder.verify(session).close();
    }

    @Test
    public void rollsBackTheTransactionOnException() throws Exception {
        executeWithException();

        final InOrder inOrder = inOrder(session, transaction);
        inOrder.verify(session).beginTransaction();
        inOrder.verify(transaction).rollback();
        inOrder.verify(session).close();
    }

    @Test
    public void doesNotCommitAnInactiveTransaction() throws Exception {
        when(transaction.getStatus()).thenReturn(NOT_ACTIVE);

        execute();

        verify(transaction, never()).commit();
    }

    @Test
    public void doesNotCommitANullTransaction() throws Exception {
        when(session.getTransaction()).thenReturn(null);

        execute();

        verify(transaction, never()).commit();
    }

    @Test
    public void doesNotRollbackAnInactiveTransaction() throws Exception {
        when(transaction.getStatus()).thenReturn(NOT_ACTIVE);

        executeWithException();

        verify(transaction, never()).rollback();
    }

    @Test
    public void doesNotRollbackANullTransaction() throws Exception {
        when(session.getTransaction()).thenReturn(null);

        executeWithException();

        verify(transaction, never()).rollback();
    }

    @Test
    public void beginsAndCommitsATransactionForAnalytics() throws Exception {
        prepareResourceMethod("methodWithUnitOfWorkOnAnalyticsDatabase");
        execute();

        final InOrder inOrder = inOrder(analyticsSession, analyticsTransaction);
        inOrder.verify(analyticsSession).beginTransaction();
        inOrder.verify(analyticsTransaction).commit();
        inOrder.verify(analyticsSession).close();
    }

    @Test
    public void throwsExceptionOnNotRegisteredDatabase() throws Exception {
        prepareResourceMethod("methodWithUnitOfWorkOnNotRegisteredDatabase");
        assertThatThrownBy(this::execute)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unregistered Hibernate bundle: 'warehouse'");
    }

    private void prepareResourceMethod(String resourceMethodName) throws NoSuchMethodException {
        final Method handlingMethod = MockResource.class.getMethod(resourceMethodName);
        Method definitionMethod = handlingMethod;
        Class<?> interfaceClass = MockResource.class.getInterfaces()[0];
        if (methodDefinedOnInterface(resourceMethodName, interfaceClass.getMethods())) {
            definitionMethod = interfaceClass.getMethod(resourceMethodName);
        }
        when(uriInfo.getMatchedResourceMethod()).thenReturn(Resource.builder()
            .addMethod()
            .handlingMethod(handlingMethod)
            .handledBy(new MockResource(), definitionMethod)
            .build());
    }

    private static boolean methodDefinedOnInterface(String methodName, Method[] methods) {
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        return false;
    }

    private void execute() {
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(responseFiltersStartEvent);
        requestListener.onEvent(responseFinishedEvent);
    }

    private void executeWithException() {
        RequestEventListener requestListener = listener.onRequest(requestStartEvent);
        requestListener.onEvent(requestMethodStartEvent);
        requestListener.onEvent(responseFiltersStartEvent);
        requestListener.onEvent(requestMethodExceptionEvent);
        requestListener.onEvent(responseFinishedEvent);
    }

    public static class MockResource implements MockResourceInterface {

        @UnitOfWork(readOnly = false, cacheMode = CacheMode.NORMAL, transactional = true, flushMode = FlushMode.AUTO)
        public void methodWithDefaultAnnotation() {
        }

        @UnitOfWork(readOnly = true, cacheMode = CacheMode.NORMAL, transactional = true, flushMode = FlushMode.AUTO)
        public void methodWithReadOnlyAnnotation() {
        }

        @UnitOfWork(readOnly = false, cacheMode = CacheMode.IGNORE, transactional = true, flushMode = FlushMode.AUTO)
        public void methodWithCacheModeIgnoreAnnotation() {
        }

        @UnitOfWork(readOnly = false, cacheMode = CacheMode.NORMAL, transactional = true, flushMode = FlushMode.ALWAYS)
        public void methodWithFlushModeAlwaysAnnotation() {
        }

        @UnitOfWork(readOnly = false, cacheMode = CacheMode.NORMAL, transactional = false, flushMode = FlushMode.AUTO)
        public void methodWithTransactionalFalseAnnotation() {
        }

        @UnitOfWork(readOnly = true)
        @Override
        public void handlingMethodAnnotated() {
        }

        @Override
        public void definitionMethodAnnotated() {
        }

        @UnitOfWork(readOnly = false)
        @Override
        public void bothMethodsAnnotated() {

        }

        @UnitOfWork("analytics")
        public void methodWithUnitOfWorkOnAnalyticsDatabase() {

        }

        @UnitOfWork("warehouse")
        public void methodWithUnitOfWorkOnNotRegisteredDatabase() {

        }
    }

    public static interface MockResourceInterface {

        void handlingMethodAnnotated();

        @UnitOfWork(readOnly = true)
        void definitionMethodAnnotated();

        @UnitOfWork(readOnly = true)
        void bothMethodsAnnotated();
    }
}
