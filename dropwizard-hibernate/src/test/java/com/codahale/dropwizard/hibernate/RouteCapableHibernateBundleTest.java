package com.codahale.dropwizard.hibernate;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.db.DataSourceRoute;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;

import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RouteCapableHibernateBundleTest {
    private static final String ROUTE_ONE = "RouteOne";
    private static final String ROUTE_TWO = "RouteTwo";
    private final DataSourceFactory dbConfigRouteOne = new DataSourceFactory();
    private final DataSourceFactory dbConfigRouteTwo = new DataSourceFactory();
    private final SessionFactory sessionFactoryRouteOne = mock(SessionFactory.class);
    private final SessionFactory sessionFactoryRouteTwo = mock(SessionFactory.class);
    private final ImmutableList<Class<?>> entities = ImmutableList.<Class<?>> of(Person.class);
    private final SessionFactoryFactory factory = mock(SessionFactoryFactory.class);
    private final Configuration configuration = mock(Configuration.class);
    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private final HibernateBundle<Configuration> bundle = new RouteCapableHibernateBundle<Configuration>(entities,
            factory) {
        @Override
        public ImmutableList<DataSourceRoute> getDataSourceRoutes(Configuration configuration) {
            final ImmutableList.Builder<DataSourceRoute> bldr = new ImmutableList.Builder<>();

            final DataSourceRoute dsRouteOne = new DataSourceRoute();
            dsRouteOne.setDatabase(dbConfigRouteOne);
            dsRouteOne.setRouteName(ROUTE_ONE);
            bldr.add(dsRouteOne);

            final DataSourceRoute dsRouteTwo = new DataSourceRoute();
            dsRouteTwo.setDatabase(dbConfigRouteTwo);
            dsRouteTwo.setRouteName(ROUTE_TWO);
            bldr.add(dsRouteTwo);
            return bldr.build();
        }
    };

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.jersey()).thenReturn(jerseyEnvironment);

        when(factory.build(eq(bundle), any(Environment.class), eq(dbConfigRouteOne), anyList(), eq(ROUTE_ONE)))
                .thenReturn(sessionFactoryRouteOne);
        when(factory.build(eq(bundle), any(Environment.class), eq(dbConfigRouteTwo), anyList(), eq(ROUTE_TWO)))
                .thenReturn(sessionFactoryRouteTwo);
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
    public void buildsSessionFactories() throws Exception {
        bundle.run(configuration, environment);

        verify(factory).build(bundle, environment, dbConfigRouteOne, entities, ROUTE_ONE);
        verify(factory).build(bundle, environment, dbConfigRouteTwo, entities, ROUTE_TWO);
    }

    @Test
    public void registersATransactionalAdapter() throws Exception {
        bundle.run(configuration, environment);

        final ArgumentCaptor<UnitOfWorkResourceMethodDispatchAdapter> captor = ArgumentCaptor
                .forClass(UnitOfWorkResourceMethodDispatchAdapter.class);
        verify(jerseyEnvironment).register(captor.capture());

        assertThat(captor.getValue().getSessionFactoryMap()).containsValue(sessionFactoryRouteOne);
        assertThat(captor.getValue().getSessionFactoryMap()).containsValue(sessionFactoryRouteTwo);
        assertThat(captor.getValue().getDefaultSessionFactory()).isEqualTo(sessionFactoryRouteOne);
    }

    @Test
    public void registersSessionFactoryHealthChecks() throws Exception {
        dbConfigRouteOne.setValidationQuery("SELECT something RouteOne");
        dbConfigRouteTwo.setValidationQuery("SELECT something RouteTwo");

        bundle.run(configuration, environment);

        final ArgumentCaptor<SessionFactoryHealthCheck> captor = ArgumentCaptor
                .forClass(SessionFactoryHealthCheck.class);
        verify(healthChecks).register(eq(ROUTE_ONE), captor.capture());
        assertThat(captor.getValue().getSessionFactory()).isEqualTo(sessionFactoryRouteOne);
        assertThat(captor.getValue().getValidationQuery()).isEqualTo("SELECT something RouteOne");

        verify(healthChecks).register(eq(ROUTE_TWO), captor.capture());
        assertThat(captor.getValue().getSessionFactory()).isEqualTo(sessionFactoryRouteTwo);
        assertThat(captor.getValue().getValidationQuery()).isEqualTo("SELECT something RouteTwo");
    }

    @Test
    public void noOpSessionFactory() throws Exception {
        bundle.run(configuration, environment);

        assertThat(bundle.getSessionFactory()).isNull();
    }
}
