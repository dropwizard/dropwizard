package io.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * An {@link ArgumentFactory} for {@link ZonedDateTime} arguments wrapped by Guava's {@link Optional}.
 */
public class GuavaOptionalZonedTimeArgumentFactory implements ArgumentFactory<Optional<ZonedDateTime>> {

    private final java.util.Optional<Calendar> calendar;

    public GuavaOptionalZonedTimeArgumentFactory() {
        calendar = java.util.Optional.empty();
    }

    public GuavaOptionalZonedTimeArgumentFactory(java.util.Optional<TimeZone> timeZone) {
        calendar = timeZone.map(GregorianCalendar::new);
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        if (value instanceof Optional) {
            final Optional<?> optionalValue = (Optional<?>) value;
            // Fall through to OptionalArgumentFactory if absent.
            // Fall through to OptionalArgumentFactory if present, but not ZonedDateTime.
            return optionalValue.isPresent() && optionalValue.get() instanceof ZonedDateTime;
        }
        return false;
    }

    @Override
    public Argument build(Class<?> expectedType, Optional<ZonedDateTime> value, StatementContext ctx) {
        // accepts guarantees that the value is present
        return new ZonedDateTimeArgument(value.orNull(), calendar);
    }
}
