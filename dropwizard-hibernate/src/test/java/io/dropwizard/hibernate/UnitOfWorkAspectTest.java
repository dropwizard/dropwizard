package io.dropwizard.hibernate;

import com.google.common.collect.ImmutableMap;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UnitOfWorkAspectTest {
    private static final String READ_ONLY = "readOnly";
    private static final String WRITE = "write";

    public class ExampleResource {
        @UnitOfWork
        void defaultOperation() {
        }

        @UnitOfWork(READ_ONLY)
        void readOnlyOperation() {
        }

        @UnitOfWork(WRITE)
        void writeOperation() {
        }
    }

    private final SessionFactory defaultSessionFactory = mock(SessionFactory.class);
    private final SessionFactory readOnlySessionFactory = mock(SessionFactory.class);
    private final SessionFactory writeSessionFactory = mock(SessionFactory.class);
    private final Session defaultSession = mock(Session.class);
    private final Session readOnlySession = mock(Session.class);
    private final Session writeSession = mock(Session.class);

    private UnitOfWorkAspect unitOfWorkAspect;
    private UnitOfWork readOnlyUnitOfWork;
    private UnitOfWork writeUnitOfWork;
    private UnitOfWork defaultUnitOfWork;

    @Before
    public void setUp() throws Exception {
        when(defaultSessionFactory.openSession()).thenReturn(defaultSession);
        when(readOnlySessionFactory.openSession()).thenReturn(readOnlySession);
        when(writeSessionFactory.openSession()).thenReturn(writeSession);

        when(defaultSessionFactory.getCurrentSession()).thenReturn(defaultSession);
        when(readOnlySessionFactory.getCurrentSession()).thenReturn(readOnlySession);
        when(writeSessionFactory.getCurrentSession()).thenReturn(writeSession);

        when(defaultSession.getSessionFactory()).thenReturn(defaultSessionFactory);
        when(readOnlySession.getSessionFactory()).thenReturn(readOnlySessionFactory);
        when(writeSession.getSessionFactory()).thenReturn(writeSessionFactory);

        Map<String, SessionFactory> sessionFactories = ImmutableMap.<String, SessionFactory>builder()
                .put(READ_ONLY, readOnlySessionFactory)
                .put(WRITE, writeSessionFactory)
                .build();

        unitOfWorkAspect = new UnitOfWorkAspect(sessionFactories);

        readOnlyUnitOfWork = ExampleResource.class
                .getDeclaredMethod("readOnlyOperation")
                .getAnnotation(UnitOfWork.class);

        writeUnitOfWork = ExampleResource.class
                .getDeclaredMethod("writeOperation")
                .getAnnotation(UnitOfWork.class);

        defaultUnitOfWork = ExampleResource.class
                .getDeclaredMethod("defaultOperation")
                .getAnnotation(UnitOfWork.class);
    }

    @After
    public void tearDown() {
        unitOfWorkAspect.onFinish();
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfUnknownBundleNameIsProvided() {
        unitOfWorkAspect = new UnitOfWorkAspect(ImmutableMap.of(READ_ONLY, readOnlySessionFactory));

        unitOfWorkAspect.beforeStart(writeUnitOfWork);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfNoBundleNameIsProvidedButManyExist() {
        unitOfWorkAspect.beforeStart(defaultUnitOfWork);
    }

    @Test
    public void usesTheOnlyBundleIfNoBundleNameIsProvided() {
        unitOfWorkAspect = new UnitOfWorkAspect(ImmutableMap.of(READ_ONLY, readOnlySessionFactory));

        unitOfWorkAspect.beforeStart(defaultUnitOfWork);

        assertThat(UnitOfWorkAspect.getCurrentSession()).isEqualTo(readOnlySession);
        assertThat(UnitOfWorkAspect.getSessionFactory()).isEqualTo(readOnlySessionFactory);
        assertThat(UnitOfWorkAspect.getUnitOfWork()).isEqualTo(defaultUnitOfWork);
    }

    @Test
    public void setsContextBeforeStart() {
        unitOfWorkAspect.beforeStart(writeUnitOfWork);

        assertThat(UnitOfWorkAspect.getCurrentSession()).isEqualTo(writeSession);
        assertThat(UnitOfWorkAspect.getSessionFactory()).isEqualTo(writeSessionFactory);
        assertThat(UnitOfWorkAspect.getUnitOfWork()).isEqualTo(writeUnitOfWork);
    }

    @Test
    public void clearsContextOnFinish() {
        unitOfWorkAspect.beforeStart(writeUnitOfWork);
        unitOfWorkAspect.onFinish();

        assertThat(UnitOfWorkAspect.getCurrentSession()).isNull();
        assertThat(UnitOfWorkAspect.getSessionFactory()).isNull();
        assertThat(UnitOfWorkAspect.getUnitOfWork()).isNull();
    }
}
