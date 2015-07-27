package io.dropwizard.hibernate;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.setup.HttpEnvironment;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HibernateBundleTest {
    private final DataSourceFactory dbConfig = new DataSourceFactory();
    private final ImmutableList<Class<?>> entities = ImmutableList.<Class<?>>of(Person.class);
    private final SessionFactoryFactory factory = mock(SessionFactoryFactory.class);
    private final SessionFactory sessionFactory = mock(SessionFactory.class);
    private final HttpConfiguration configuration = mock(HttpConfiguration.class);
    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final HttpEnvironment environment = mock(HttpEnvironment.class);
    private final HibernateBundle<HttpConfiguration> bundle = new HibernateBundle<HttpConfiguration>(entities, factory) {
        @Override
        public DataSourceFactory getDataSourceFactory(HttpConfiguration configuration) {
            return dbConfig;
        }
    };

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.jersey()).thenReturn(jerseyEnvironment);

        when(factory.build(eq(bundle),
                           any(Environment.class),
                           any(DataSourceFactory.class),
                           anyList(),
                           eq("hibernate"))).thenReturn(sessionFactory);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addsHibernateSupportToJackson() throws Exception {
        final ObjectMapper objectMapperFactory = mock(ObjectMapper.class);

        final Bootstrap<HttpConfiguration> bootstrap = mock(Bootstrap.class);
        when(bootstrap.getObjectMapper()).thenReturn(objectMapperFactory);

        bundle.initialize(bootstrap);

        final ArgumentCaptor<Module> captor = ArgumentCaptor.forClass(Module.class);
        verify(objectMapperFactory).registerModule(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(Hibernate4Module.class);
    }

    @Test
    public void buildsASessionFactory() throws Exception {
        bundle.run(configuration, environment);

        verify(factory).build(bundle, environment, dbConfig, entities, "hibernate");
    }

    @Test
    public void registersATransactionalListener() throws Exception {
        bundle.run(configuration, environment);

        final ArgumentCaptor<UnitOfWorkApplicationListener> captor =
                ArgumentCaptor.forClass(UnitOfWorkApplicationListener.class);
        verify(jerseyEnvironment).register(captor.capture());
    }

    @Test
    public void registersASessionFactoryHealthCheck() throws Exception {
        dbConfig.setValidationQuery("SELECT something");

        bundle.run(configuration, environment);

        final ArgumentCaptor<SessionFactoryHealthCheck> captor =
                ArgumentCaptor.forClass(SessionFactoryHealthCheck.class);
        verify(healthChecks).register(eq("hibernate"), captor.capture());

        assertThat(captor.getValue().getSessionFactory()).isEqualTo(sessionFactory);

        assertThat(captor.getValue().getValidationQuery()).isEqualTo("SELECT something");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void registersACustomNameOfHealthCheckAndDBPoolMetrics() throws Exception {
        final HibernateBundle<HttpConfiguration> customBundle = new HibernateBundle<HttpConfiguration>(entities, factory) {
            @Override
            public DataSourceFactory getDataSourceFactory(HttpConfiguration configuration) {
                return dbConfig;
            }

            @Override
            protected String name() {
                return "custom-hibernate";
            }
        };
        when(factory.build(eq(customBundle),
                any(Environment.class),
                any(DataSourceFactory.class),
                anyList(),
                eq("custom-hibernate"))).thenReturn(sessionFactory);

        customBundle.run(configuration, environment);

        final ArgumentCaptor<SessionFactoryHealthCheck> captor =
                ArgumentCaptor.forClass(SessionFactoryHealthCheck.class);
        verify(healthChecks).register(eq("custom-hibernate"), captor.capture());
    }

    @Test
    public void hasASessionFactory() throws Exception {
        bundle.run(configuration, environment);

        assertThat(bundle.getSessionFactory()).isEqualTo(sessionFactory);
    }
}
