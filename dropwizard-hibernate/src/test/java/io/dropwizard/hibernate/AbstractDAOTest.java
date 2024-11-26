package io.dropwizard.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.query.Query;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("deprecation")
class AbstractDAOTest {
    private static class MockDAO extends AbstractDAO<String> {
        MockDAO(SessionFactory factory) {
            super(factory);
        }

        @Override
        public Session currentSession() {
            return super.currentSession();
        }

        @Override
        public Query<?> namedQuery(String queryName) throws HibernateException {
            return super.namedQuery(queryName);
        }

        @Override
        protected Query<String> namedTypedQuery(String queryName) throws HibernateException {
            return super.namedTypedQuery(queryName);
        }

        @Override
        public Class<String> getEntityClass() {
            return super.getEntityClass();
        }

        @Override
        public String uniqueResult(Query<String> query) throws HibernateException {
            return super.uniqueResult(query);
        }

        @Override
        public List<String> list(Query<String> query) throws HibernateException {
            return super.list(query);
        }

        @Override
        public String get(Object id) {
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
    private final HibernateCriteriaBuilder criteriaBuilder = mock(HibernateCriteriaBuilder.class);
    @SuppressWarnings("unchecked")
    private final JpaCriteriaQuery<String> criteriaQuery = mock(JpaCriteriaQuery.class);
    @SuppressWarnings("unchecked")
    private final Query<String> query = mock(Query.class);
    private final Session session = mock(Session.class);
    private final MockDAO dao = new MockDAO(factory);

    @BeforeEach
    void setup() throws Exception {
        when(criteriaBuilder.createQuery(same(String.class))).thenReturn(criteriaQuery);
        when(factory.getCurrentSession()).thenReturn(session);
        when(session.getCriteriaBuilder()).thenReturn(criteriaBuilder);
        when(session.getNamedQuery(anyString())).thenReturn(query);
        when(session.createQuery(anyString(), same(String.class))).thenReturn(query);
        when(session.createNamedQuery(anyString(), same(String.class))).thenReturn(query);
    }

    @Test
    void getsASessionFromTheSessionFactory() {
        assertThat(dao.currentSession())
                .isSameAs(session);
    }

    @Test
    void hasAnEntityClass() {
        assertThat(dao.getEntityClass())
                .isEqualTo(String.class);
    }

    @Test
    void getsNamedQueries() {
        assertThat(dao.namedQuery("query-name"))
                .isEqualTo(query);

        verify(session).getNamedQuery("query-name");
    }

    @Test
    void getsNamedTypedQueries() {
        assertThat(dao.namedTypedQuery("query-name"))
            .isEqualTo(query);

        verify(session).createNamedQuery("query-name", String.class);
    }

    @Test
    void getsTypedQueries() {
        assertThat(dao.query("HQL"))
            .isEqualTo(query);

        verify(session).createQuery("HQL", String.class);
    }

    @Test
    void createsNewCriteriaQueries() {
        assertThat(dao.criteriaQuery())
                .isEqualTo(criteriaQuery);

        verify(session).getCriteriaBuilder();
        verify(criteriaBuilder).createQuery(String.class);
    }

    @Test
    void returnsUniqueResultsFromJpaCriteriaQueries() {
        when(session.createQuery(criteriaQuery)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList("woo"));

        assertThat(dao.uniqueResult(criteriaQuery))
            .isEqualTo("woo");
    }

    @Test
    void throwsOnNonUniqueResultsFromJpaCriteriaQueries() {
        when(session.createQuery(criteriaQuery)).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList("woo", "boo"));

        assertThatExceptionOfType(NonUniqueResultException.class).isThrownBy(() ->
            dao.uniqueResult(criteriaQuery));
    }

    @Test
    void returnsUniqueResultsFromQueries() {
        when(query.uniqueResult()).thenReturn("woo");

        assertThat(dao.uniqueResult(query))
                .isEqualTo("woo");
    }

    @Test
    void returnsUniqueListsFromJpaCriteriaQueries() {
        when(session.createQuery(criteriaQuery)).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList("woo"));

        assertThat(dao.list(criteriaQuery))
            .containsOnly("woo");
    }

    @Test
    void returnsUniqueListsFromQueries() {
        when(query.list()).thenReturn(Collections.singletonList("woo"));

        assertThat(dao.list(query))
                .containsOnly("woo");
    }

    @Test
    void getsEntitiesById() {
        when(session.get(String.class, 200)).thenReturn("woo!");

        assertThat(dao.get(200))
                .isEqualTo("woo!");

        verify(session).get(String.class, 200);
    }

    @Test
    void persistsEntities() {
        assertThat(dao.persist("woo"))
                .isEqualTo("woo");

        verify(session).saveOrUpdate("woo");
    }

    @Test
    void initializesProxies() throws Exception {
        final LazyInitializer initializer = mock(LazyInitializer.class);
        when(initializer.isUninitialized()).thenReturn(true);
        final HibernateProxy proxy = mock(HibernateProxy.class);
        doCallRealMethod().when(proxy).asHibernateProxy();
        when(proxy.getHibernateLazyInitializer()).thenReturn(initializer);

        dao.initialize(proxy);

        verify(initializer).initialize();
    }
}
