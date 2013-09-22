package io.dropwizard.hibernate;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class HibernateBundleTest {
    private final DataSourceFactory dbConfig = new DataSourceFactory();
    private final ImmutableList<Class<?>> entities = ImmutableList.<Class<?>>of(Person.class);
    private final SessionFactoryFactory factory = mock(SessionFactoryFactory.class);
    private final SessionFactory sessionFactory = mock(SessionFactory.class);
    private final Configuration configuration = mock(Configuration.class);
    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private final HibernateBundle<Configuration> bundle = new HibernateBundle<Configuration>(entities, factory) {
        @Override
        public DataSourceFactory getDataSourceFactory(Configuration configuration) {
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
                           anyList())).thenReturn(sessionFactory);
    }

    @Test
    public void addsHibernateSupportToJackson() throws Exception {
        final ObjectMapper objectMapperFactory = mock(ObjectMapper.class);

        final Bootstrap<?> bootstrap = mock(Bootstrap.class);
        when(bootstrap.getObjectMapper()).thenReturn(objectMapperFactory);

        bundle.initialize(bootstrap);

        final ArgumentCaptor<Module> captor = ArgumentCaptor.forClass(Module.class);
        verify(objectMapperFactory).registerModule(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(Hibernate4Module.class);
    }

    @Test
    public void buildsASessionFactory() throws Exception {
        bundle.run(configuration, environment);

        verify(factory).build(bundle, environment, dbConfig, entities);
    }

    @Test
    public void registersATransactionalAdapter() throws Exception {
        bundle.run(configuration, environment);

        final ArgumentCaptor<UnitOfWorkResourceMethodDispatchAdapter> captor =
                ArgumentCaptor.forClass(UnitOfWorkResourceMethodDispatchAdapter.class);
        verify(jerseyEnvironment).register(captor.capture());

        assertThat(captor.getValue().getSessionFactory()).isEqualTo(sessionFactory);
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
    public void hasASessionFactory() throws Exception {
        bundle.run(configuration, environment);

        assertThat(bundle.getSessionFactory()).isEqualTo(sessionFactory);
    }
}
