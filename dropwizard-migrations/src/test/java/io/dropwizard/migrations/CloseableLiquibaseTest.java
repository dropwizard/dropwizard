package io.dropwizard.migrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedPooledDataSource;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;

@Execution(SAME_THREAD)
class CloseableLiquibaseTest {

    CloseableLiquibase liquibase;
    ManagedPooledDataSource dataSource;

    @BeforeEach
    void setUp() throws Exception {
        DataSourceFactory factory = new DataSourceFactory();

        factory.setDriverClass(org.h2.Driver.class.getName());
        factory.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis());
        factory.setUser("DbTest");

        dataSource = (ManagedPooledDataSource) factory.build(new MetricRegistry(), "DbTest");
        liquibase = new CloseableLiquibaseWithClassPathMigrationsFile(dataSource, "migrations.xml");
    }

    @Test
    void testWhenClosingAllConnectionsInPoolIsReleased() throws Exception {

        ConnectionPool pool = dataSource.getPool();
        assertThat(pool.getActive()).isEqualTo(1);

        liquibase.close();

        assertThat(pool.getActive()).isZero();
        assertThat(pool.getIdle()).isZero();
        assertThat(pool.isClosed()).isTrue();
    }
}
