package io.dropwizard.hibernate;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedPooledDataSource;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.common.BootstrapLogging;
import org.jspecify.annotations.Nullable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.internal.EmptyInterceptor;
import org.hibernate.service.ServiceRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SessionFactoryFactoryTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final SessionFactoryFactory factory = new SessionFactoryFactory();

    private final HibernateBundle<?> bundle = mock(HibernateBundle.class);
    private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private final MetricRegistry metricRegistry = new MetricRegistry();

    private final DataSourceFactory config = new DataSourceFactory();

    @Nullable
    private SessionFactory sessionFactory;

    @BeforeEach
    void setUp() throws Exception {
        when(environment.metrics()).thenReturn(metricRegistry);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);

        config.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis());
        config.setUser("sa");
        config.setDriverClass("org.h2.Driver");
        config.setValidationQuery("SELECT 1");

        final Map<String, String> properties = Map.of(
            "hibernate.show_sql", "true",
            "hibernate.dialect", "org.hibernate.dialect.H2Dialect",
            "hibernate.jdbc.time_zone", "UTC");
        config.setProperties(properties);
    }

    @AfterEach
    void tearDown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    void managesTheSessionFactory() {
        build();

        verify(lifecycleEnvironment).manage(any(SessionFactoryManager.class));
    }

    @Test
    void callsBundleToConfigure() {
        build();

        verify(bundle).configure(any(Configuration.class));
    }

    @Test
    void setsPoolName() {
        build();

        ArgumentCaptor<SessionFactoryManager> sessionFactoryManager = ArgumentCaptor.forClass(SessionFactoryManager.class);
        verify(lifecycleEnvironment).manage(sessionFactoryManager.capture());
        assertThat(sessionFactoryManager.getValue().getDataSource())
            .isInstanceOfSatisfying(ManagedPooledDataSource.class, dataSource ->
                assertThat(dataSource.getPool().getName()).isEqualTo("hibernate"));
    }

    @Test
    void setsACustomPoolName() {
        this.sessionFactory = factory.build(bundle, environment, config,
            Collections.singletonList(Person.class), "custom-hibernate-db");

        ArgumentCaptor<SessionFactoryManager> sessionFactoryManager = ArgumentCaptor.forClass(SessionFactoryManager.class);
        verify(lifecycleEnvironment).manage(sessionFactoryManager.capture());
        assertThat(sessionFactoryManager.getValue().getDataSource())
            .isInstanceOfSatisfying(ManagedPooledDataSource.class, dataSource ->
                assertThat(dataSource.getPool().getName()).isEqualTo("custom-hibernate-db"));
    }

    @Test
    void buildsAWorkingSessionFactory() {
        build();

        try (Session session = requireNonNull(sessionFactory).openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createNativeQuery("DROP TABLE people IF EXISTS").executeUpdate();
            session.createNativeQuery("CREATE TABLE people (name varchar(100) primary key, email varchar(100), birthday timestamp(0))").executeUpdate();
            session.createNativeQuery("INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00')").executeUpdate();
            transaction.commit();

            final Person entity = session.get(Person.class, "Coda");

            assertThat(entity.getName())
                .isEqualTo("Coda");

            assertThat(entity.getEmail())
                .isEqualTo("coda@example.com");

            assertThat(requireNonNull(entity.getBirthday()))
                .isEqualTo(ZonedDateTime.of(1979, 1, 2, 0, 22, 0, 0, ZoneId.of("UTC")));
        }
    }

    @Test
    void configureRunsBeforeSessionFactoryCreation() {
        final SessionFactoryFactory customFactory = new SessionFactoryFactory() {
            @Override
            protected void configure(Configuration configuration, ServiceRegistry registry) {
                super.configure(configuration, registry);
                configuration.setInterceptor(EmptyInterceptor.INSTANCE);
            }
        };
        sessionFactory = customFactory.build(bundle,
            environment,
            config,
            Collections.singletonList(Person.class));

        assertThat(sessionFactory.getSessionFactoryOptions().getInterceptor()).isSameAs(EmptyInterceptor.INSTANCE);
    }

    @Test
    void buildBootstrapServiceRegistryRunsBeforeSessionFactoryCreation() {
        final SessionFactoryFactory customFactory = new SessionFactoryFactory() {
            @Override
            protected BootstrapServiceRegistryBuilder configureBootstrapServiceRegistryBuilder(BootstrapServiceRegistryBuilder builder) {
                return builder;
            }
        };
        sessionFactory = customFactory.build(bundle,
            environment,
            config,
            Collections.singletonList(Person.class));

        assertThat(sessionFactory.getSessionFactoryOptions().getInterceptor()).isSameAs(EmptyInterceptor.INSTANCE);
    }

    private void build() {
        this.sessionFactory = factory.build(bundle,
            environment,
            config,
            Collections.singletonList(Person.class));
    }
}
