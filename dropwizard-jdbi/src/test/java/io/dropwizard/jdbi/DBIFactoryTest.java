package io.dropwizard.jdbi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Deque;
import java.util.LinkedList;

import org.junit.Test;
import org.mockito.Mockito;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.tweak.ArgumentFactory;
import org.skife.jdbi.v2.tweak.ContainerFactory;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jdbi.args.GuavaOptionalArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalInstantArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalJodaTimeArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalLocalDateArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalLocalDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalOffsetTimeArgumentFactory;
import io.dropwizard.jdbi.args.GuavaOptionalZonedTimeArgumentFactory;
import io.dropwizard.jdbi.args.InstantArgumentFactory;
import io.dropwizard.jdbi.args.InstantMapper;
import io.dropwizard.jdbi.args.JodaDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.JodaDateTimeMapper;
import io.dropwizard.jdbi.args.LocalDateArgumentFactory;
import io.dropwizard.jdbi.args.LocalDateMapper;
import io.dropwizard.jdbi.args.LocalDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.LocalDateTimeMapper;
import io.dropwizard.jdbi.args.OffsetDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.OffsetDateTimeMapper;
import io.dropwizard.jdbi.args.OptionalArgumentFactory;
import io.dropwizard.jdbi.args.OptionalDoubleArgumentFactory;
import io.dropwizard.jdbi.args.OptionalDoubleMapper;
import io.dropwizard.jdbi.args.OptionalInstantArgumentFactory;
import io.dropwizard.jdbi.args.OptionalIntArgumentFactory;
import io.dropwizard.jdbi.args.OptionalIntMapper;
import io.dropwizard.jdbi.args.OptionalJodaTimeArgumentFactory;
import io.dropwizard.jdbi.args.OptionalLocalDateArgumentFactory;
import io.dropwizard.jdbi.args.OptionalLocalDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.OptionalLongArgumentFactory;
import io.dropwizard.jdbi.args.OptionalLongMapper;
import io.dropwizard.jdbi.args.OptionalOffsetDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.OptionalZonedDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.ZonedDateTimeArgumentFactory;
import io.dropwizard.jdbi.args.ZonedDateTimeMapper;

public class DBIFactoryTest {

    @Test
    public void testDefaultConfiguration() {

        final DBI dbi = mock(DBI.class);
        final PooledDataSourceFactory configuration = mock(PooledDataSourceFactory.class);

        // Capture what is configured
        final Deque<Class<?>> af = new LinkedList<>();
        final Deque<Class<?>> cm = new LinkedList<>();
        final Deque<Class<?>> cf = new LinkedList<>();

        Mockito.doAnswer(invocation -> {
            final ArgumentFactory<?> x = invocation.getArgument(0);
            af.addLast(x.getClass());

            return null;
        }).when(dbi).registerArgumentFactory(Mockito.isA(ArgumentFactory.class));

        Mockito.doAnswer(invocation -> {
            final ResultColumnMapper<?> x = invocation.getArgument(0);
            cm.addLast(x.getClass());

            return null;
        }).when(dbi).registerColumnMapper(Mockito.isA(ResultColumnMapper.class));

        Mockito.doAnswer(invocation -> {
            final ContainerFactory<?> x = invocation.getArgument(0);
            cf.addLast(x.getClass());

            return null;
        }).when(dbi).registerContainerFactory(Mockito.isA(ContainerFactory.class));

        when(configuration.getDriverClass()).thenReturn("io.dropwizard.fake.driver.Driver");

        final DBIFactory test = new DBIFactory();
        test.configure(dbi, configuration);

        // Verify we actually captured something
        assertFalse(cm.isEmpty());
        assertFalse(cf.isEmpty());
        assertFalse(af.isEmpty());

        // Verify Argument Factory Order
        assertEquals(GuavaOptionalArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalDoubleArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalIntArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalLongArgumentFactory.class, af.removeFirst());

        assertEquals(JodaDateTimeArgumentFactory.class, af.removeFirst());
        assertEquals(LocalDateArgumentFactory.class, af.removeFirst());
        assertEquals(LocalDateTimeArgumentFactory.class, af.removeFirst());
        assertEquals(InstantArgumentFactory.class, af.removeFirst());
        assertEquals(OffsetDateTimeArgumentFactory.class, af.removeFirst());
        assertEquals(ZonedDateTimeArgumentFactory.class, af.removeFirst());

        assertEquals(GuavaOptionalJodaTimeArgumentFactory.class, af.removeFirst());
        assertEquals(GuavaOptionalLocalDateArgumentFactory.class, af.removeFirst());
        assertEquals(GuavaOptionalLocalDateTimeArgumentFactory.class, af.removeFirst());
        assertEquals(GuavaOptionalInstantArgumentFactory.class, af.removeFirst());
        assertEquals(GuavaOptionalOffsetTimeArgumentFactory.class, af.removeFirst());
        assertEquals(GuavaOptionalZonedTimeArgumentFactory.class, af.removeFirst());

        assertEquals(OptionalJodaTimeArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalLocalDateArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalLocalDateTimeArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalInstantArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalOffsetDateTimeArgumentFactory.class, af.removeFirst());
        assertEquals(OptionalZonedDateTimeArgumentFactory.class, af.removeFirst());

        // Verify Column Mapper Order
        assertEquals(OptionalDoubleMapper.class, cm.removeFirst());
        assertEquals(OptionalIntMapper.class, cm.removeFirst());
        assertEquals(OptionalLongMapper.class, cm.removeFirst());

        assertEquals(JodaDateTimeMapper.class, cm.removeFirst());
        assertEquals(LocalDateMapper.class, cm.removeFirst());
        assertEquals(LocalDateTimeMapper.class, cm.removeFirst());
        assertEquals(InstantMapper.class, cm.removeFirst());
        assertEquals(OffsetDateTimeMapper.class, cm.removeFirst());
        assertEquals(ZonedDateTimeMapper.class, cm.removeFirst());

        // Verify Container Factory Order
        assertEquals(ImmutableListContainerFactory.class, cf.removeFirst());
        assertEquals(ImmutableSetContainerFactory.class, cf.removeFirst());
        assertEquals(GuavaOptionalContainerFactory.class, cf.removeFirst());
        assertEquals(OptionalContainerFactory.class, cf.removeFirst());

        // Verify we have accounted for everything
        assertTrue(cm.isEmpty());
        assertTrue(cf.isEmpty());
        assertTrue(af.isEmpty());
    }

}
