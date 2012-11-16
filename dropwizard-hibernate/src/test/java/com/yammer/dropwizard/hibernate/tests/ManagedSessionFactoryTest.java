package com.yammer.dropwizard.hibernate.tests;

import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.hibernate.ManagedSessionFactory;
import org.hibernate.SessionFactory;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ManagedSessionFactoryTest {
    private final SessionFactory factory = mock(SessionFactory.class);
    private final ManagedDataSource dataSource = mock(ManagedDataSource.class);
    private final ManagedSessionFactory managedFactory = new ManagedSessionFactory(factory,
                                                                                   dataSource);

    @Test
    public void stoppingTheFactoryClosesIt() throws Exception {
        managedFactory.stop();

        verify(factory).close();
    }

    @Test
    public void stoppingTheFactoryStopsTheDataSource() throws Exception {
        managedFactory.stop();

        verify(dataSource).stop();
    }
}
