package io.dropwizard.hibernate;

import com.google.common.collect.ImmutableList;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractDAOTest {
    private static class MockDAO extends AbstractDAO<String> {
        MockDAO(SessionFactory factory) {
            super(factory);
        }

        @Override
        public Session currentSession() {
            return super.currentSession();
        }

        @Override
        public Criteria criteria() {
            return super.criteria();
        }

        @Override
        public Query namedQuery(String queryName) throws HibernateException {
            return super.namedQuery(queryName);
        }

        @Override
        public Class<String> getEntityClass() {
            return super.getEntityClass();
        }

        @Override
        public String uniqueResult(Criteria criteria) throws HibernateException {
            return super.uniqueResult(criteria);
        }

        @Override
        public String uniqueResult(Query<String> query) throws HibernateException {
            return super.uniqueResult(query);
        }

        @Override
        public List<String> list(Criteria criteria) throws HibernateException {
            return super.list(criteria);
        }

        @Override
        public List<String> list(Query<String> query) throws HibernateException {
            return super.list(query);
        }

        @Override
        public String get(Serializable id) {
            return super.get(id);
        }

        @Override
        public String persist(String entity) throws HibernateException {
            return super.persist(entity);
        }

        @Override
        public <T> T initialize(T proxy) {
            return super.initialize(proxy);
        }
    }

    private final SessionFactory factory = mock(SessionFactory.class);
    private final Criteria criteria = mock(Criteria.class);
    @SuppressWarnings("unchecked")
    private final CriteriaQuery<String> criteriaQuery = mock(CriteriaQuery.class);
    @SuppressWarnings("unchecked")
    private final Query<String> query = mock(Query.class);
    private final Session session = mock(Session.class);
    private final MockDAO dao = new MockDAO(factory);

    @Before
    public void setup() throws Exception {
        when(factory.getCurrentSession()).thenReturn(session);
        when(session.createCriteria(String.class)).thenReturn(criteria);
        when(session.getNamedQuery(anyString())).thenReturn(query);
        when(session.createQuery(anyString(), same(String.class))).thenReturn(query);
    }

    @Test
    public void getsASessionFromTheSessionFactory() throws Exception {
        assertThat(dao.currentSession())
                .isSameAs(session);
    }

    @Test
    public void hasAnEntityClass() throws Exception {
        assertThat(dao.getEntityClass())
                .isEqualTo(String.class);
    }

    @Test
    public void getsNamedQueries() throws Exception {
        assertThat(dao.namedQuery("query-name"))
                .isEqualTo(query);

        verify(session).getNamedQuery("query-name");
    }

    @Test
    public void getsTypedQueries() throws Exception {
        assertThat(dao.query("HQL"))
            .isEqualTo(query);

        verify(session).createQuery("HQL", String.class);
    }

    @Test
    public void createsNewCriteriaQueries() throws Exception {
        assertThat(dao.criteria())
                .isEqualTo(criteria);

        verify(session).createCriteria(String.class);
    }

    @Test
    public void returnsUniqueResultsFromCriteriaQueries() throws Exception {
        when(criteria.uniqueResult()).thenReturn("woo");

        assertThat(dao.uniqueResult(criteria))
                .isEqualTo("woo");
    }

    @Test
    public void returnsUniqueResultsFromJpaCriteriaQueries() throws Exception {
        when(session.createQuery(criteriaQuery)).thenReturn(query);
        when(query.getResultList()).thenReturn(ImmutableList.of("woo"));

        assertThat(dao.uniqueResult(criteriaQuery))
            .isEqualTo("woo");
    }

    @Test(expected = NonUniqueResultException.class)
    public void throwsOnNonUniqueResultsFromJpaCriteriaQueries() throws Exception {
        when(session.createQuery(criteriaQuery)).thenReturn(query);
        when(query.getResultList()).thenReturn(ImmutableList.of("woo", "boo"));

        dao.uniqueResult(criteriaQuery);
    }

    @Test
    public void returnsUniqueResultsFromQueries() throws Exception {
        when(query.uniqueResult()).thenReturn("woo");

        assertThat(dao.uniqueResult(query))
                .isEqualTo("woo");
    }

    @Test
    public void returnsUniqueListsFromCriteriaQueries() throws Exception {
        when(criteria.list()).thenReturn(ImmutableList.of("woo"));

        assertThat(dao.list(criteria))
                .containsOnly("woo");
    }

    @Test
    public void returnsUniqueListsFromJpaCriteriaQueries() throws Exception {
        when(session.createQuery(criteriaQuery)).thenReturn(query);
        when(query.getResultList()).thenReturn(ImmutableList.of("woo"));

        assertThat(dao.list(criteriaQuery))
            .containsOnly("woo");
    }

    @Test
    public void returnsUniqueListsFromQueries() throws Exception {
        when(query.list()).thenReturn(ImmutableList.of("woo"));

        assertThat(dao.list(query))
                .containsOnly("woo");
    }

    @Test
    public void getsEntitiesById() throws Exception {
        when(session.get(String.class, 200)).thenReturn("woo!");

        assertThat(dao.get(200))
                .isEqualTo("woo!");

        verify(session).get(String.class, 200);
    }

    @Test
    public void persistsEntities() throws Exception {
        assertThat(dao.persist("woo"))
                .isEqualTo("woo");

        verify(session).saveOrUpdate("woo");
    }

    @Test
    public void initializesProxies() throws Exception {
        final LazyInitializer initializer = mock(LazyInitializer.class);
        when(initializer.isUninitialized()).thenReturn(true);
        final HibernateProxy proxy = mock(HibernateProxy.class);
        when(proxy.getHibernateLazyInitializer()).thenReturn(initializer);

        dao.initialize(proxy);

        verify(initializer).initialize();
    }
}
