package io.dropwizard.hibernate;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.setup.Environment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnitOfWorkAwareProxyFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    private SessionFactory sessionFactory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        final HibernateBundle<?> bundle = mock(HibernateBundle.class);
        final Environment environment = mock(Environment.class);
        when(environment.lifecycle()).thenReturn(mock(LifecycleEnvironment.class));
        when(environment.metrics()).thenReturn(new MetricRegistry());

        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setUrl("jdbc:hsqldb:mem:unit-of-work-" + UUID.randomUUID().toString());
        dataSourceFactory.setUser("sa");
        dataSourceFactory.setDriverClass("org.hsqldb.jdbcDriver");
        dataSourceFactory.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
        dataSourceFactory.setProperties(ImmutableMap.of("hibernate.dialect", "org.hibernate.dialect.HSQLDialect"));
        dataSourceFactory.setInitialSize(1);
        dataSourceFactory.setMinSize(1);

        sessionFactory = new SessionFactoryFactory()
                .build(bundle, environment, dataSourceFactory, ImmutableList.of());
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createNativeQuery("create table user_sessions (token varchar(64) primary key, username varchar(16))")
                .executeUpdate();
            session.createNativeQuery("insert into user_sessions values ('67ab89d', 'jeff_28')")
                .executeUpdate();
            transaction.commit();
        }
    }

    @Test
    public void testProxyWorks() throws Exception {
        final SessionDao sessionDao = new SessionDao(sessionFactory);
        final UnitOfWorkAwareProxyFactory unitOfWorkAwareProxyFactory =
                new UnitOfWorkAwareProxyFactory("default", sessionFactory);

        final OAuthAuthenticator oAuthAuthenticator = unitOfWorkAwareProxyFactory
                .create(OAuthAuthenticator.class, SessionDao.class, sessionDao);
        assertThat(oAuthAuthenticator.authenticate("67ab89d")).isTrue();
        assertThat(oAuthAuthenticator.authenticate("bd1e23a")).isFalse();
    }

    @Test
    public void testProxyWorksWithoutUnitOfWork() {
        assertThat(new UnitOfWorkAwareProxyFactory("default", sessionFactory)
                .create(PlainAuthenticator.class)
                .authenticate("c82d11e"))
                .isTrue();
    }

    @Test
    public void testProxyHandlesErrors() {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Session cluster is down");

        new UnitOfWorkAwareProxyFactory("default", sessionFactory)
                .create(BrokenAuthenticator.class)
                .authenticate("b812ae4");
    }

    @Test
    public void testNewAspect() {
        final UnitOfWorkAwareProxyFactory unitOfWorkAwareProxyFactory =
                new UnitOfWorkAwareProxyFactory("default", sessionFactory);

        ImmutableMap<String, SessionFactory> sessionFactories = ImmutableMap.of("default", sessionFactory);
        UnitOfWorkAspect aspect1 = unitOfWorkAwareProxyFactory.newAspect(sessionFactories);
        UnitOfWorkAspect aspect2 = unitOfWorkAwareProxyFactory.newAspect(sessionFactories);
        assertThat(aspect1).isNotSameAs(aspect2);
    }

    @Test
    public void testCanBeConfiguredWithACustomAspect() {
        final SessionDao sessionDao = new SessionDao(sessionFactory);
        final UnitOfWorkAwareProxyFactory unitOfWorkAwareProxyFactory =
            new UnitOfWorkAwareProxyFactory("default", sessionFactory) {
                @Override
                public UnitOfWorkAspect newAspect(ImmutableMap<String, SessionFactory> sessionFactories) {
                    return new CustomAspect(sessionFactories);
                }
            };

        final OAuthAuthenticator oAuthAuthenticator = unitOfWorkAwareProxyFactory
            .create(OAuthAuthenticator.class, SessionDao.class, sessionDao);
        assertThat(oAuthAuthenticator.authenticate("gr6f9y0")).isTrue();
    }

    static class SessionDao {

        private SessionFactory sessionFactory;

        public SessionDao(SessionFactory sessionFactory) {
            this.sessionFactory = sessionFactory;
        }

        public boolean isExist(String token) {
            return sessionFactory.getCurrentSession()
                    .createNativeQuery("select username from user_sessions where token=:token")
                    .setParameter("token", token)
                    .list()
                    .size() > 0;
        }

    }

    static class OAuthAuthenticator {

        private SessionDao sessionDao;

        public OAuthAuthenticator(SessionDao sessionDao) {
            this.sessionDao = sessionDao;
        }

        @UnitOfWork
        public boolean authenticate(String token) {
            return sessionDao.isExist(token);
        }
    }

    static class PlainAuthenticator {

        public boolean authenticate(String token) {
            return true;
        }
    }

    static class BrokenAuthenticator {

        @UnitOfWork
        public boolean authenticate(String token) {
            throw new IllegalStateException("Session cluster is down");
        }
    }

    static class CustomAspect extends UnitOfWorkAspect {
        public CustomAspect(Map<String, SessionFactory> sessionFactories) {
            super(sessionFactories);
        }

        @Override
        protected void configureSession() {
            super.configureSession();
            Transaction transaction = getSession().beginTransaction();
            getSession().createNativeQuery("insert into user_sessions values ('gr6f9y0', 'jeff_29')")
                .executeUpdate();
            transaction.commit();
        }
    }
}
