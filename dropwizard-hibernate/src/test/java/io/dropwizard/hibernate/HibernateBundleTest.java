package io.dropwizard.hibernate;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HibernateBundleTest {
    private final DataSourceFactory dbConfig = new DataSourceFactory();
    private final List<Class<?>> entities = Collections.singletonList(Person.class);
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

    @BeforeEach
    void setUp() throws Exception {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(jerseyEnvironment.getResourceConfig()).thenReturn(new DropwizardResourceConfig());


        when(factory.build(eq(bundle),
                           any(Environment.class),
                           any(DataSourceFactory.class),
                           anyList(),
                           eq("hibernate"))).thenReturn(sessionFactory);
    }

    @Test
    void addsHibernateSupportToJackson() throws Exception {
        final ObjectMapper objectMapperFactory = mock(ObjectMapper.class);

        final Bootstrap<?> bootstrap = mock(Bootstrap.class);
        when(bootstrap.getObjectMapper()).thenReturn(objectMapperFactory);

        bundle.initialize(bootstrap);

        final ArgumentCaptor<Module> captor = ArgumentCaptor.forClass(Module.class);
        verify(objectMapperFactory).registerModule(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(Hibernate5JakartaModule.class);
    }

    @Test
    void buildsASessionFactory() throws Exception {
        bundle.run(configuration, environment);

        verify(factory).build(bundle, environment, dbConfig, entities, "hibernate");
    }

    @Test
    void registersATransactionalListener() throws Exception {
        bundle.run(configuration, environment);

        final ArgumentCaptor<UnitOfWorkApplicationListener> captor =
                ArgumentCaptor.forClass(UnitOfWorkApplicationListener.class);
        verify(jerseyEnvironment).register(captor.capture());
    }

    @Test
    void registersASessionFactoryHealthCheck() throws Exception {
        dbConfig.setValidationQuery("SELECT something");

        bundle.run(configuration, environment);

        final ArgumentCaptor<SessionFactoryHealthCheck> captor =
                ArgumentCaptor.forClass(SessionFactoryHealthCheck.class);
        verify(healthChecks).register(eq("hibernate"), captor.capture());

        assertThat(captor.getValue().getSessionFactory()).isEqualTo(sessionFactory);

        assertThat(captor.getValue().getValidationQuery()).isEqualTo(Optional.of("SELECT something"));
    }

    @Test
    void registersACustomNameOfHealthCheckAndDBPoolMetrics() throws Exception {
        final HibernateBundle<Configuration> customBundle = new HibernateBundle<Configuration>(entities, factory) {
            @Override
            public DataSourceFactory getDataSourceFactory(Configuration configuration) {
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
    void hasASessionFactory() throws Exception {
        bundle.run(configuration, environment);

        assertThat(bundle.getSessionFactory()).isEqualTo(sessionFactory);
    }
}
