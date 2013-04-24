package com.codahale.dropwizard.db.tests;

import com.codahale.metrics.MetricRegistry;
import com.codahale.dropwizard.db.DatabaseConfiguration;
import com.codahale.dropwizard.db.ManagedDataSource;
import com.codahale.dropwizard.db.ManagedDataSourceFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.fest.assertions.api.Assertions.assertThat;

public class ManagedDataSourceFactoryTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final ManagedDataSourceFactory factory = new ManagedDataSourceFactory();

    private ManagedDataSource dataSource;

    @Before
    public void setUp() throws Exception {
        final DatabaseConfiguration config = new DatabaseConfiguration();
        config.setUrl("jdbc:hsqldb:mem:DbTest-" + System.currentTimeMillis());
        config.setUser("sa");
        config.setDriverClass("org.hsqldb.jdbcDriver");
        config.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        this.dataSource = factory.build(metricRegistry, config);
    }

    @After
    public void tearDown() throws Exception {
        dataSource.stop();
    }

    @Test
    public void buildsAConnectionPoolToTheDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS")) {
                try (ResultSet set = statement.executeQuery()) {
                    while (set.next()) {
                        assertThat(set.getInt(1)).isEqualTo(1);
                    }
                }
            }
        }
    }
}
