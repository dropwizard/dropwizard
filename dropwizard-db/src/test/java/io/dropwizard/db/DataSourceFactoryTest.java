package io.dropwizard.db;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class DataSourceFactoryTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();

    private DataSourceFactory factory;
    private ManagedDataSource dataSource;

    @Before
    public void setUp() {
        factory = new DataSourceFactory();
        factory.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis() + ";user=sa");
        factory.setDriverClass("org.h2.Driver");
        factory.setValidationQuery("SELECT 1");
    }

    @After
    public void tearDown() throws Exception {
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
    public void buildsAConnectionPoolToTheDatabase() throws Exception {
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
    public void testNoValidationQueryTimeout() throws Exception {
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                assertThat(statement.getQueryTimeout()).isEqualTo(0);
            }
        }
    }

    @Test
    public void testValidationQueryTimeoutIsSet() throws Exception {
        factory.setValidationQueryTimeout(Duration.seconds(3));

        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                assertThat(statement.getQueryTimeout()).isEqualTo(3);
            }
        }
    }

    @Test(expected = SQLException.class)
    public void invalidJDBCDriverClassThrowsSQLException() throws SQLException {
        final DataSourceFactory factory = new DataSourceFactory();
        factory.setDriverClass("org.example.no.driver.here");

        factory.build(metricRegistry, "test").getConnection();
    }

    @Test
    public void testCustomValidator() throws Exception {
        factory.setValidatorClassName(Optional.of(CustomConnectionValidator.class.getName()));
        try (Connection connection = dataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement("select 1")) {
                try (ResultSet rs = statement.executeQuery()) {
                    assertThat(rs.next());
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
            }
        }
        assertThat(CustomConnectionValidator.loaded).isTrue();
    }

    @Test
    public void createDefaultFactory() throws Exception {
        final DataSourceFactory factory = new ConfigurationFactory<>(DataSourceFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new ResourceConfigurationSourceProvider(), "yaml/minimal_db_pool.yml");

        assertThat(factory.getDriverClass()).isEqualTo("org.postgresql.Driver");
        assertThat(factory.getUser()).isEqualTo("pg-user");
        assertThat(factory.getPassword()).isEqualTo("iAMs00perSecrEET");
        assertThat(factory.getUrl()).isEqualTo("jdbc:postgresql://db.example.com/db-prod");
        assertThat(factory.getValidationQuery()).isEqualTo("/* Health Check */ SELECT 1");
        assertThat(factory.getValidationQueryTimeout()).isEqualTo(Optional.empty());
    }
}
