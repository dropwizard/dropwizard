package com.yammer.dropwizard.db.tests;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.db.ManagedDataSourceFactory;
import com.yammer.dropwizard.db.ManagedPooledDataSource;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.extractProperty;
import static org.junit.Assert.assertEquals;

public class ManagedDataSourceFactoryTest {
    private final ManagedDataSourceFactory factory = new ManagedDataSourceFactory();

    private ManagedDataSource dataSource;
    private DatabaseConfiguration config;

    @Before
    public void setUp() throws Exception {
        config = new DatabaseConfiguration();
        config.setUrl("jdbc:hsqldb:mem:DbTest-" + System.currentTimeMillis());
        config.setUser("sa");
        config.setDriverClass("org.hsqldb.jdbcDriver");
        config.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        this.dataSource = factory.build(config);
    }

    @After
    public void tearDown() throws Exception {
        dataSource.stop();
        Metrics.defaultRegistry().shutdown();
    }

    @Test
    public void buildsAConnectionPoolToTheDatabase() throws Exception {
        final Connection connection = dataSource.getConnection();
        try {
            final PreparedStatement statement = connection.prepareStatement("select 1 from INFORMATION_SCHEMA.SYSTEM_USERS");
            try {
                final ResultSet set = statement.executeQuery();
                try {
                    while (set.next()) {
                        assertThat(set.getInt(1)).isEqualTo(1);
                    }
                } finally {
                    set.close();
                }
            } finally {
                statement.close();
            }
        } finally {
            connection.close();
        }
    }

    /** Extract any Gauges created by ManagedDataSourceFactory */
    private SortedMap<MetricName, Metric> getDataSourceGauges() {
        final String className = ManagedPooledDataSource.class.getName();
        SortedMap<String, SortedMap<MetricName, Metric>> gauges = Metrics.defaultRegistry().groupedMetrics(new MetricPredicate() {
            @Override
            public boolean matches(MetricName name, Metric metric) {
                final String fullName = name.getGroup() + "." + name.getType();
                return metric instanceof Gauge && fullName.equals(className);
            }
        });

        //flatten the map
        SortedMap<MetricName, Metric> results = new TreeMap<MetricName, Metric>();
        for (SortedMap<MetricName, Metric> map : gauges.values()) {
            results.putAll(map);
        }
        return results;
    }

    @Test
    public void createsGauges() throws Exception {
        SortedMap<MetricName, Metric> gauges = getDataSourceGauges();
        assertEquals(2, gauges.size());

        assertThat(extractProperty("name").from(gauges.keySet())).contains("numActive", "numIdle");
    }
}
