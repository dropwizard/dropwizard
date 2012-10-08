package com.yammer.dropwizard.db.tests;

import com.yammer.dropwizard.db.ManagedPooledDataSource;
import org.apache.tomcat.dbcp.pool.ObjectPool;
import org.junit.Test;

import java.sql.SQLFeatureNotSupportedException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class ManagedPooledDataSourceTest {
    private final ObjectPool pool = mock(ObjectPool.class);
    private final ManagedPooledDataSource dataSource = new ManagedPooledDataSource(pool);

    @Test
    public void isAlreadyStarted() throws Exception {
        dataSource.start();

        verifyZeroInteractions(pool);
    }

    @Test
    public void closesThePoolWhenStopped() throws Exception {
        dataSource.stop();

        verify(pool).close();
    }

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
