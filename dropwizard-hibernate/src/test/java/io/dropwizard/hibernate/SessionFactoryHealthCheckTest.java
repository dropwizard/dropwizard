package io.dropwizard.hibernate;

import org.jspecify.annotations.Nullable;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.ReturningWork;
import org.hibernate.query.NativeQuery;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.resource.transaction.spi.TransactionStatus.ACTIVE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("HibernateResourceOpenedButNotSafelyClosed")
class SessionFactoryHealthCheckTest {
    private final SessionFactory factory = mock(SessionFactory.class);

    @Test
    void hasASessionFactory() throws Exception {
        assertThat(healthCheck().getSessionFactory())
                .isEqualTo(factory);
    }

    @Test
    void hasAValidationQuery() throws Exception {
        assertThat(healthCheck("SELECT 1").getValidationQuery())
                .isEqualTo(Optional.of("SELECT 1"));
    }

    @Test
    void isHealthyIfNoExceptionIsThrown() throws Exception {
        final Session session = mock(Session.class);
        when(factory.openSession()).thenReturn(session);

        final Transaction transaction = mock(Transaction.class);
        when(session.beginTransaction()).thenReturn(transaction);

        final NativeQuery<?> query = mock(NativeQuery.class);
        when(session.createNativeQuery(anyString())).thenReturn(query);

        assertThat(healthCheck("SELECT 1").execute().isHealthy()).isTrue();

        final InOrder inOrder = inOrder(factory, session, transaction, query);
        inOrder.verify(factory).openSession();
        inOrder.verify(session).beginTransaction();
        inOrder.verify(session).createNativeQuery("SELECT 1");
        inOrder.verify(query).list();
        inOrder.verify(transaction).commit();
        inOrder.verify(session).close();
    }

    @Test
    void isHealthyIfIsValid() {
        final Session session = mock(Session.class);
        when(factory.openSession()).thenReturn(session);

        final Transaction transaction = mock(Transaction.class);
        when(session.beginTransaction()).thenReturn(transaction);

        when(session.doReturningWork(any(ReturningWork.class))).thenReturn(true);

        assertThat(healthCheck().execute().isHealthy()).isTrue();

        final InOrder inOrder = inOrder(factory, session, transaction);
        inOrder.verify(factory).openSession();
        inOrder.verify(session).beginTransaction();
        inOrder.verify(session).doReturningWork(any(ReturningWork.class));
        inOrder.verify(transaction).commit();
        inOrder.verify(session).close();
    }

    @Test
    void isUnhealthyIfAnExceptionIsThrown() throws Exception {
        final Session session = mock(Session.class);
        when(factory.openSession()).thenReturn(session);

        final Transaction transaction = mock(Transaction.class);
        when(session.beginTransaction()).thenReturn(transaction);
        when(transaction.getStatus()).thenReturn(ACTIVE);

        final NativeQuery<?> query = mock(NativeQuery.class);
        when(session.createNativeQuery(anyString())).thenReturn(query);
        when(query.list()).thenThrow(new HibernateException("OH NOE"));

        assertThat(healthCheck("SELECT 1").execute().isHealthy())
                .isFalse();

        final InOrder inOrder = inOrder(factory, session, transaction, query);
        inOrder.verify(factory).openSession();
        inOrder.verify(session).beginTransaction();
        inOrder.verify(session).createNativeQuery("SELECT 1");
        inOrder.verify(query).list();
        inOrder.verify(transaction).rollback();
        inOrder.verify(session).close();

        verify(transaction, never()).commit();
    }

    @Test
    void isUnhealthyIfIsNotValid() {
        final Session session = mock(Session.class);
        when(factory.openSession()).thenReturn(session);

        final Transaction transaction = mock(Transaction.class);
        when(session.beginTransaction()).thenReturn(transaction);

        when(session.doReturningWork(any(ReturningWork.class))).thenReturn(false);

        assertThat(healthCheck().execute().isHealthy()).isFalse();

        final InOrder inOrder = inOrder(factory, session, transaction);
        inOrder.verify(factory).openSession();
        inOrder.verify(session).beginTransaction();
        inOrder.verify(session).doReturningWork(any(ReturningWork.class));
        inOrder.verify(transaction).commit();
        inOrder.verify(session).close();
    }

    private SessionFactoryHealthCheck healthCheck() {
        return healthCheck(null);
    }

    private SessionFactoryHealthCheck healthCheck(@Nullable String validationQuery) {
        return new SessionFactoryHealthCheck(factory, Optional.ofNullable(validationQuery));
    }

}
