package com.yammer.dropwizard.hibernate.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractDAOTest {
    private static class MockDAO extends AbstractDAO<String> {
        MockDAO(SessionFactory factory) {
            super(factory, String.class);
        }

        public Session getSession() {
            return currentSession();
        }

        public Criteria getCriteria() {
            return criteria();
        }

        public String getUniqueResultFromCriteria() {
            return uniqueResult(criteria());
        }

        public Query getNamedQuery(String name) {
            return namedQuery(name);
        }

        public String getUniqueResultFromQuery(String name) {
            return uniqueResult(namedQuery(name));
        }

        public List<String> getListFromCriteria() {
            return list(criteria());
        }

        public List<String> getListFromQuery(String name) {
            return list(namedQuery(name));
        }

        public String getInstance(Integer i) {
            return get(i);
        }

        public String save(String entity) {
            return persist(entity);
        }
    }

    private final SessionFactory factory = mock(SessionFactory.class);
    private final Criteria criteria = mock(Criteria.class);
    private final Query query = mock(Query.class);
    private final Session session = mock(Session.class);
    private final MockDAO dao = new MockDAO(factory);

    @Before
    public void setup() throws Exception {
        when(factory.getCurrentSession()).thenReturn(session);
        when(session.createCriteria(String.class)).thenReturn(criteria);
        when(session.getNamedQuery(anyString())).thenReturn(query);
    }

    @Test
    public void getsASessionFromTheSessionFactory() throws Exception {
        assertThat(dao.getSession())
                .isSameAs(session);
    }

    @Test
    public void hasAnEntityClass() throws Exception {
        assertThat(dao.getEntityClass())
                .isEqualTo(String.class);
    }

    @Test
    public void getsNamedQueries() throws Exception {
        assertThat(dao.getNamedQuery("query-name"))
                .isEqualTo(query);

        verify(session).getNamedQuery("query-name");
    }

    @Test
    public void createsNewCriteriaQueries() throws Exception {
        assertThat(dao.getCriteria())
                .isEqualTo(criteria);

        verify(session).createCriteria(String.class);
    }

    @Test
    public void returnsUniqueResultsFromCriteriaQueries() throws Exception {
        when(criteria.uniqueResult()).thenReturn("woo");

        assertThat(dao.getUniqueResultFromCriteria())
                .isEqualTo("woo");

        verify(session).createCriteria(String.class);
        verify(criteria).uniqueResult();
    }

    @Test
    public void returnsUniqueResultsFromQueries() throws Exception {
        when(query.uniqueResult()).thenReturn("woo");

        assertThat(dao.getUniqueResultFromQuery("query-name"))
                .isEqualTo("woo");

        verify(session).getNamedQuery("query-name");
        verify(query).uniqueResult();
    }

    @Test
    public void returnsUniqueListsFromCriteriaQueries() throws Exception {
        when(criteria.list()).thenReturn(ImmutableList.of("woo"));

        assertThat(dao.getListFromCriteria())
                .containsOnly("woo");

        verify(session).createCriteria(String.class);
        verify(criteria).list();
    }


    @Test
    public void returnsUniqueListsFromQueries() throws Exception {
        when(query.list()).thenReturn(ImmutableList.of("woo"));

        assertThat(dao.getListFromQuery("query-name"))
                .containsOnly("woo");

        verify(session).getNamedQuery("query-name");
        verify(query).list();
    }

    @Test
    public void itGetsEntitiesById() throws Exception {
        when(session.get(String.class, 200)).thenReturn("woo!");

        assertThat(dao.getInstance(200))
                .isEqualTo("woo!");

        verify(session).get(String.class, 200);
    }

    @Test
    public void itPersistsEntities() throws Exception {
        assertThat(dao.save("woo"))
                .isEqualTo("woo");

        verify(session).saveOrUpdate("woo");
    }

}
