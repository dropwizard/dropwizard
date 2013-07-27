package com.codahale.dropwizard.db;

import com.codahale.metrics.MetricRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.fest.assertions.api.Assertions.assertThat;

public class DataSourceFactoryTest {
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final DataSourceFactory factory = new DataSourceFactory();

    private ManagedDataSource dataSource;

    @Before
    public void setUp() throws Exception {
        factory.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis());
        factory.setUser("sa");
        factory.setDriverClass("org.h2.Driver");
        factory.setValidationQuery("SELECT 1");

        this.dataSource = factory.build(metricRegistry, "test");
        dataSource.start();
    }

    @After
    public void tearDown() throws Exception {
        dataSource.stop();
    }

    @Test
    public void buildsAConnectionPoolToTheDatabase() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "select 1")) {
                try (ResultSet set = statement.executeQuery()) {
                    while (set.next()) {
                        assertThat(set.getInt(1)).isEqualTo(1);
                    }
                }
            }
        }
    }
}
