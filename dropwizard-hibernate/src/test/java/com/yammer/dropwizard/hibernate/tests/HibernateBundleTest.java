package com.yammer.dropwizard.hibernate.tests;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.hibernate.HibernateBundle;
import com.yammer.dropwizard.hibernate.SessionFactoryFactory;
import com.yammer.dropwizard.hibernate.SessionFactoryHealthCheck;
import com.yammer.dropwizard.hibernate.UnitOfWorkResourceMethodDispatchAdapter;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HibernateBundleTest {
    private final DatabaseConfiguration dbConfig = new DatabaseConfiguration();
    private final ImmutableList<Class<?>> entities = ImmutableList.<Class<?>>of(Person.class);
    private final SessionFactoryFactory factory = mock(SessionFactoryFactory.class);
    private final SessionFactory sessionFactory = mock(SessionFactory.class);
    private final Configuration configuration = mock(Configuration.class);
    private final Environment environment = mock(Environment.class);
    private final HibernateBundle<Configuration> bundle = new HibernateBundle<Configuration>(entities, factory) {
        @Override
        public DatabaseConfiguration getDatabaseConfiguration(Configuration configuration) {
            return dbConfig;
        }
    };

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        when(factory.build(any(Environment.class),
                           any(DatabaseConfiguration.class),
                           anyList())).thenReturn(sessionFactory);
    }

    @Test
    public void addsHibernateSupportToJackson() throws Exception {
        final ObjectMapperFactory objectMapperFactory = mock(ObjectMapperFactory.class);

        final Bootstrap<?> bootstrap = mock(Bootstrap.class);
        when(bootstrap.getObjectMapperFactory()).thenReturn(objectMapperFactory);

        bundle.initialize(bootstrap);

        final ArgumentCaptor<Module> captor = ArgumentCaptor.forClass(Module.class);
        verify(objectMapperFactory).registerModule(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(Hibernate4Module.class);
    }

    @Test
    public void buildsASessionFactory() throws Exception {
        bundle.run(configuration, environment);

        verify(factory).build(environment, dbConfig, entities);
    }

    @Test
    public void registersATransactionalAdapter() throws Exception {
        bundle.run(configuration, environment);

        final ArgumentCaptor<UnitOfWorkResourceMethodDispatchAdapter> captor =
                ArgumentCaptor.forClass(UnitOfWorkResourceMethodDispatchAdapter.class);
        verify(environment).addProvider(captor.capture());

        assertThat(captor.getValue().getSessionFactory()).isEqualTo(sessionFactory);
    }

    @Test
    public void registersASessionFactoryHealthCheck() throws Exception {
        dbConfig.setValidationQuery("SELECT something");

        bundle.run(configuration, environment);

        final ArgumentCaptor<SessionFactoryHealthCheck> captor =
                ArgumentCaptor.forClass(SessionFactoryHealthCheck.class);
        verify(environment).addHealthCheck(captor.capture());

        assertThat(captor.getValue().getSessionFactory()).isEqualTo(sessionFactory);

        assertThat(captor.getValue().getValidationQuery()).isEqualTo("SELECT something");
    }

    @Test
    public void hasASessionFactory() throws Exception {
        bundle.run(configuration, environment);

        assertThat(bundle.getSessionFactory()).isEqualTo(sessionFactory);
    }
}
