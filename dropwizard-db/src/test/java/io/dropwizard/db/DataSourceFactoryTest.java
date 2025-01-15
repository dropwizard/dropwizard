package io.dropwizard.db;

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;
import org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;

class DataSourceFactoryTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();

    private DataSourceFactory factory;

    @Nullable
    private ManagedDataSource dataSource;

    @BeforeEach
    void setUp() {
        factory = new DataSourceFactory();
        factory.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis() + ";user=sa");
        factory.setDriverClass("org.h2.Driver");
        factory.setValidationQuery("SELECT 1");
    }

    @AfterEach
    void tearDown() throws Exception {
        if (null != dataSource) {
            dataSource.stop();
        }
    }

    private ManagedDataSource dataSource() throws Exception {
        dataSource = factory.build(metricRegistry, "test");
        dataSource.start();
        return dataSource;
    }

    @Test
    void testEmptyDriverClass() {
        factory.setDriverClass(null);
        ManagedDataSource dataSource = factory.build(metricRegistry, "test");
        assertThatNoException().isThrownBy(dataSource::start);
    }

    @Test
    void testInitialSizeIsZero() {
        factory.setUrl("nonsense invalid url");
        factory.setInitialSize(0);
        ManagedDataSource dataSource = factory.build(metricRegistry, "test");
        assertThatNoException().isThrownBy(dataSource::start);
    }

    @Test
    void buildsAConnectionPoolToTheDatabase() throws Exception {
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                try (ResultSet set = statement.executeQuery()) {
                    while (set.next()) {
                        assertThat(set.getInt(1)).isEqualTo(1);
                    }
                }
            }
        }
    }

    @Test
    void testNoValidationQueryTimeout() throws Exception {
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                assertThat(statement.getQueryTimeout()).isZero();
            }
        }
    }

    @Test
    void testValidationQueryTimeoutIsSet() throws Exception {
        factory.setValidationQueryTimeout(Duration.seconds(3));

        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                assertThat(statement.getQueryTimeout()).isEqualTo(3);
            }
        }
    }

    @Test
    void invalidJDBCDriverClassThrowsSQLException() {
        final DataSourceFactory factory = new DataSourceFactory();
        factory.setDriverClass("org.example.no.driver.here");

        assertThatExceptionOfType(SQLException.class).isThrownBy(() ->
            factory.build(metricRegistry, "test").getConnection());
    }

    @Test
    void testCustomValidator() throws Exception {
        factory.setValidatorClassName(Optional.of(CustomConnectionValidator.class.getName()));
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                try (ResultSet rs = statement.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
            }
        }
        assertThat(CustomConnectionValidator.loaded).isTrue();
    }

    @Test
    void testJdbcInterceptors() throws Exception {
        factory.setJdbcInterceptors(Optional.of("StatementFinalizer;ConnectionState"));

        assertThat(dataSource())
            .isInstanceOfSatisfying(ManagedPooledDataSource.class, source ->
                assertThat(source.getPoolProperties().getJdbcInterceptorsAsArray())
                .extracting("interceptorClass")
                .contains(StatementFinalizer.class, ConnectionState.class));
    }

    @Test
    void createDefaultFactory() throws Exception {
        final DataSourceFactory factory = new YamlConfigurationFactory<>(DataSourceFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new ResourceConfigurationSourceProvider(), "yaml/minimal_db_pool.yml");

        assertThat(factory.getDriverClass()).isEqualTo("org.postgresql.Driver");
        assertThat(factory.getUser()).isEqualTo("pg-user");
        assertThat(factory.getPassword()).isEqualTo("iAMs00perSecrEET");
        assertThat(factory.getUrl()).isEqualTo("jdbc:postgresql://db.example.com/db-prod");
        assertThat(factory.getValidationQuery()).isEqualTo(Optional.of("/* Health Check */ SELECT 1"));
        assertThat(factory.getValidationQueryTimeout()).isNotPresent();
    }

    @Test
    void metricsRecorded() throws Exception {
        dataSource();
        assertThat(metricRegistry.getGauges(MetricFilter.startsWith("io.dropwizard.db.ManagedPooledDataSource.test.")))
            .containsOnlyKeys(
                "io.dropwizard.db.ManagedPooledDataSource.test.active",
                "io.dropwizard.db.ManagedPooledDataSource.test.idle",
                "io.dropwizard.db.ManagedPooledDataSource.test.waiting",
                "io.dropwizard.db.ManagedPooledDataSource.test.size",
                "io.dropwizard.db.ManagedPooledDataSource.test.created",
                "io.dropwizard.db.ManagedPooledDataSource.test.borrowed",
                "io.dropwizard.db.ManagedPooledDataSource.test.reconnected",
                "io.dropwizard.db.ManagedPooledDataSource.test.released",
                "io.dropwizard.db.ManagedPooledDataSource.test.releasedIdle",
                "io.dropwizard.db.ManagedPooledDataSource.test.returned",
                "io.dropwizard.db.ManagedPooledDataSource.test.removeAbandoned");
    }

}
