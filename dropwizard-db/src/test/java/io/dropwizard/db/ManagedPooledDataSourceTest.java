package io.dropwizard.db;

import com.codahale.metrics.MetricRegistry;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.junit.Test;

import java.sql.SQLFeatureNotSupportedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class ManagedPooledDataSourceTest {
    private final PoolProperties config = new PoolProperties();
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private final ManagedPooledDataSource dataSource = new ManagedPooledDataSource(config, metricRegistry);

    @Test
    public void hasNoParentLogger() throws Exception {
        try {
            dataSource.getParentLogger();
            failBecauseExceptionWasNotThrown(SQLFeatureNotSupportedException.class);
        } catch (SQLFeatureNotSupportedException e) {
            assertThat((Object) e).isInstanceOf(SQLFeatureNotSupportedException.class);
        }
    }
}
