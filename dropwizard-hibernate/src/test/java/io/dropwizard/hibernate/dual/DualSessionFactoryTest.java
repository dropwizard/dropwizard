package io.dropwizard.hibernate.dual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit test class that verifies the DualSessionFactory wrapper.
 *
 * @since 2.1
 *
 */
public class DualSessionFactoryTest {

    private static final Session primary = mock(Session.class);
    private static final SessionFactory primaryFactory = mock(SessionFactory.class);
    private static final Session reader = mock(Session.class);
    private static final SessionFactory readerFactory = mock(SessionFactory.class);

    @BeforeAll
    public static void beforeAll() throws Exception {
        when(primaryFactory.getCurrentSession()).thenReturn(primary);
        when(readerFactory.getCurrentSession()).thenReturn(reader);
    }

    public static Stream<Arguments> current() {
        return Stream.of(arguments(false, primaryFactory), arguments(true, readerFactory));
    }

    @ParameterizedTest
    @MethodSource
    public void current(final boolean readOnly, final SessionFactory expected) {
        try (final DualSessionFactory factory = new DualSessionFactory(primaryFactory, readerFactory)) {
            factory.prepare(readOnly);
            assertThat(factory.current()).isEqualTo(expected);
        }
    }

    public static Stream<Arguments> getCurrentSession() {
        return Stream.of(arguments(false, primary), arguments(true, reader));
    }

    @ParameterizedTest
    @MethodSource
    public void getCurrentSession(final boolean readOnly, final Session expected) {
        try (final DualSessionFactory factory = new DualSessionFactory(primaryFactory, readerFactory)) {
            factory.prepare(readOnly);
            assertThat(factory.getCurrentSession()).isEqualTo(expected);
        }
    }

    public static Stream<Arguments> prepare() {
        return Stream.of(arguments(false, primaryFactory), arguments(true, readerFactory));
    }

    @ParameterizedTest
    @MethodSource
    public void prepare(final boolean readOnly, final SessionFactory expected) {
        try (final DualSessionFactory factory = new DualSessionFactory(primaryFactory, readerFactory)) {
            assertThat(factory.prepare(readOnly)).isEqualTo(expected);
        }
    }
}
