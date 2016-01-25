package io.dropwizard.hibernate;

import com.codahale.metrics.health.HealthCheck;
import org.hibernate.*;
import org.junit.Test;
import org.mockito.InOrder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import static org.hibernate.resource.transaction.spi.TransactionStatus.ACTIVE;

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

        final SQLQuery query = mock(SQLQuery.class);
        when(session.createSQLQuery(anyString())).thenReturn(query);

        assertThat(healthCheck.execute())
                .isEqualTo(HealthCheck.Result.healthy());

        final InOrder inOrder = inOrder(factory, session, transaction, query);
        inOrder.verify(factory).openSession();
        inOrder.verify(session).beginTransaction();
        inOrder.verify(session).createSQLQuery("SELECT 1");
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

        final SQLQuery query = mock(SQLQuery.class);
        when(session.createSQLQuery(anyString())).thenReturn(query);
        when(query.list()).thenThrow(new HibernateException("OH NOE"));

        assertThat(healthCheck.execute().isHealthy())
                .isFalse();

        final InOrder inOrder = inOrder(factory, session, transaction, query);
        inOrder.verify(factory).openSession();
        inOrder.verify(session).beginTransaction();
        inOrder.verify(session).createSQLQuery("SELECT 1");
        inOrder.verify(query).list();
        inOrder.verify(transaction).rollback();
        inOrder.verify(session).close();

        verify(transaction, never()).commit();
    }
}
