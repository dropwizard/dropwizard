package io.dropwizard.hibernate;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedPooledDataSource;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Maps;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ClusteredSessionFactoryFactoryTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final ClusteredSessionFactoryFactory factory = new ClusteredSessionFactoryFactory();

    private final HibernateBundle<?> bundle = mock(HibernateBundle.class);
    private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private final MetricRegistry metricRegistry = new MetricRegistry();

    private DataSourceFactory config = new DataSourceFactory();

    @Nullable
    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        when(environment.metrics()).thenReturn(metricRegistry);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);

        config.setUrl("jdbc:hsqldb:mem:DbTest-" + System.currentTimeMillis());
        config.setUser("sa");
        config.setDriverClass("org.hsqldb.jdbcDriver");
        config.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        final Map<String, String> properties = Maps.of(
            "hibernate.show_sql", "true",
            "hibernate.dialect", "org.hibernate.dialect.HSQLDialect",
            "hibernate.jdbc.time_zone", "UTC");
        config.setProperties(properties);
    }

    @After
    public void tearDown() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void managesTheSessionFactory() throws Exception {
        build();

        verify(lifecycleEnvironment).manage(any(SessionFactoryManager.class));
    }

    @Test
    public void callsBundleToConfigure() throws Exception {
        build();

        verify(bundle).configure(any(Configuration.class));
    }

    @Test
    public void setsPoolName() {
        build();

        ArgumentCaptor<SessionFactoryManager> sessionFactoryManager = ArgumentCaptor.forClass(SessionFactoryManager.class);
        verify(lifecycleEnvironment).manage(sessionFactoryManager.capture());
        ManagedPooledDataSource dataSource = (ManagedPooledDataSource) sessionFactoryManager.getValue().getDataSource();
        assertThat(dataSource.getPool().getName()).isEqualTo("hibernate");
    }

    @Test
    public void setsACustomPoolName() {
        this.sessionFactory = factory.build(bundle, environment, config,
                Collections.singletonList(Person.class), "custom-hibernate-db").getSessionFactory();

        ArgumentCaptor<SessionFactoryManager> sessionFactoryManager = ArgumentCaptor.forClass(SessionFactoryManager.class);
        verify(lifecycleEnvironment).manage(sessionFactoryManager.capture());
        ManagedPooledDataSource dataSource = (ManagedPooledDataSource) sessionFactoryManager.getValue().getDataSource();
        assertThat(dataSource.getPool().getName()).isEqualTo("custom-hibernate-db");
    }

    @Test
    public void buildsAWorkingSessionFactory() throws Exception {
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

            assertThat(requireNonNull(entity.getBirthday()).toDateTime(DateTimeZone.UTC))
                .isEqualTo(new DateTime(1979, 1, 2, 0, 22, DateTimeZone.UTC));
        }
    }

    @Test
    public void configureRunsBeforeSessionFactoryCreation() {
        final ClusteredSessionFactoryFactory customFactory = new ClusteredSessionFactoryFactory() {
            @Override
            protected void configure(Configuration configuration, ServiceRegistry registry) {
                super.configure(configuration, registry);
                configuration.setInterceptor(EmptyInterceptor.INSTANCE);
            }
        };
        sessionFactory = customFactory.build(bundle,
                                             environment,
                                             config,
                                             Collections.singletonList(Person.class)).getSessionFactory();

        assertThat(sessionFactory.getSessionFactoryOptions().getInterceptor()).isSameAs(EmptyInterceptor.INSTANCE);
    }

    private void build() {
        this.sessionFactory = factory.build(bundle,
                                            environment,
                                            config,
                                            Collections.singletonList(Person.class)).getSessionFactory();
    }
}
