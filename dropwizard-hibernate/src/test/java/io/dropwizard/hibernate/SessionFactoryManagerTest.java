package io.dropwizard.hibernate;

import io.dropwizard.db.ManagedDataSource;
import org.hibernate.SessionFactory;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class SessionFactoryManagerTest {
    private final SessionFactory factory = mock(SessionFactory.class);
    private final ManagedDataSource dataSource = mock(ManagedDataSource.class);
    private final SessionFactoryManager manager = new SessionFactoryManager(factory, dataSource);

    @Test
    public void closesTheFactoryOnStopping() throws Exception {
        manager.stop();

        verify(factory).close();
    }

    @Test
    public void stopsTheDataSourceOnStopping() throws Exception {
        manager.stop();

        verify(dataSource).stop();
    }

    @Test
    public void startsTheDataSourceOnStarting() throws Exception {
        manager.start();

        verify(dataSource).start();
    }
}
