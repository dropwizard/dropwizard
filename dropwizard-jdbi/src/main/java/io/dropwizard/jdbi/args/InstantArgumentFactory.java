package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * An {@link ArgumentFactory} for {@link Instant} arguments.
 */
public class InstantArgumentFactory implements ArgumentFactory<Instant> {
    /**
     * <p>{@link Calendar} for representing a database time zone.<p>
     * If a field is not represented in a database as
     * {@code TIMESTAMP WITH TIME ZONE}, we need to set its time zone
     * explicitly. Otherwise it will not be correctly represented in
     * a time zone different from the time zone of the database.
     */
    private final Optional<Calendar> calendar;

    public InstantArgumentFactory() {
        this(Optional.empty());
    }

    public InstantArgumentFactory(final Optional<TimeZone> tz) {
        this.calendar = tz.map(GregorianCalendar::new);
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Instant;
    }

    @Override
    public Argument build(Class<?> expectedType, Instant value, StatementContext ctx) {
        return new InstantArgument(value, calendar);
    }
}
