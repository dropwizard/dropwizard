package io.dropwizard.hibernate;


import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ClusteredSessionFactoryTest {
    private SessionFactory writer = mock(SessionFactory.class);
    private SessionFactory reader = mock(SessionFactory.class);
    private ClusteredSessionFactory factory;

    @Before
    public void setUp() throws Exception {
        factory = new ClusteredSessionFactory(writer, reader);
    }

    @Test
    public void getWriterByDefault() throws Exception {
        assertThat(factory.getSessionFactory()).isEqualTo(writer);
    }

    @Test
    public void getsReaderWhenReadOnlyVariableIsSet() throws Exception {
        factory.setReadOnly(true);

        assertThat(factory.getSessionFactory()).isEqualTo(reader);
    }

    @Test
    public void settingIsPerThread() throws Exception {
        factory.setReadOnly(true);
        AtomicBoolean testPasses = new AtomicBoolean(true);

        Thread thread = new Thread(() -> assertThat(factory.getSessionFactory()).isEqualTo(writer));
        thread.setUncaughtExceptionHandler((t, e) -> testPasses.set(false));

        thread.start();
        thread.join();

        assertThat(testPasses.get()).as("Expected to get the write session factory on the other thread").isTrue();
    }
}
