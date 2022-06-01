package io.dropwizard.db;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.codahale.metrics.MetricRegistry;
import java.sql.SQLFeatureNotSupportedException;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.jupiter.api.Test;

class ManagedPooledDataSourceTest {
    private final PoolProperties config = new PoolProperties();
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final ManagedPooledDataSource dataSource = new ManagedPooledDataSource(config, metricRegistry);

    @Test
    void hasNoParentLogger() {
        assertThatExceptionOfType(SQLFeatureNotSupportedException.class).isThrownBy(dataSource::getParentLogger);
    }
}
