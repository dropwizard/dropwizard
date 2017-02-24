package io.dropwizard.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.junit.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hibernate.resource.transaction.spi.TransactionStatus.ACTIVE;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("HibernateResourceOpenedButNotSafelyClosed")
public class SessionFactoryHealthCheckTest {
    private final SessionFactory factory = mock(SessionFactory.class);
    private final SessionFactoryHealthCheck healthCheck = new SessionFactoryHealthCheck(factory,
                                                                                        "SELECT 1");

    @Test
    public void hasASessionFactory() throws Exception {
        assertThat(healthCheck.getSessionFactory())
                .isEqualTo(factory);
    }

    @Test
    public void hasAValidationQuery() throws Exception {
        assertThat(healthCheck.getValidationQuery())
                .isEqualTo("SELECT 1");
    }

    @Test
    public void isHealthyIfNoExceptionIsThrown() throws Exception {
        final Session session = mock(Session.class);
        when(factory.openSession()).thenReturn(session);

        final Transaction transaction = mock(Transaction.class);
        when(session.beginTransaction()).thenReturn(transaction);

        final NativeQuery query = mock(NativeQuery.class);
        when(session.createNativeQuery(anyString())).thenReturn(query);

        assertThat(healthCheck.execute().isHealthy()).isTrue();

        final InOrder inOrder = inOrder(factory, session, transaction, query);
        inOrder.verify(factory).openSession();
        inOrder.verify(session).beginTransaction();
        inOrder.verify(session).createNativeQuery("SELECT 1");
        inOrder.verify(query).list();
        inOrder.verify(transaction).commit();
        inOrder.verify(session).close();
    }

    @Test
    public void isUnhealthyIfAnExceptionIsThrown() throws Exception {
        final Session session = mock(Session.class);
        when(factory.openSession()).thenReturn(session);

        final Transaction transaction = mock(Transaction.class);
        when(session.beginTransaction()).thenReturn(transaction);
        when(transaction.getStatus()).thenReturn(ACTIVE);

        final NativeQuery query = mock(NativeQuery.class);
        when(session.createNativeQuery(anyString())).thenReturn(query);
        when(query.list()).thenThrow(new HibernateException("OH NOE"));

        assertThat(healthCheck.execute().isHealthy())
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
}
