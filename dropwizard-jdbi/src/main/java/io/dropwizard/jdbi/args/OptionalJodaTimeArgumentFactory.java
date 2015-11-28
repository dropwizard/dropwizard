package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * An {@link ArgumentFactory} for Joda's {@link DateTime} arguments wrapped by {@link Optional}.
 */
public class OptionalJodaTimeArgumentFactory implements ArgumentFactory<Optional<DateTime>> {

    private final Optional<Calendar> calendar;

    public OptionalJodaTimeArgumentFactory() {
        calendar = Optional.empty();
    }

    public OptionalJodaTimeArgumentFactory(Optional<TimeZone> timeZone) {
        calendar = timeZone.map(GregorianCalendar::new);
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        if (value instanceof Optional) {
            final Optional<?> optionalValue = (Optional<?>) value;
            // Fall through to OptionalArgumentFactory if absent.
            // Fall through to OptionalArgumentFactory if present, but not DateTime.
            return optionalValue.isPresent() && optionalValue.get() instanceof DateTime;
        }
        return false;
    }

    @Override
    public Argument build(Class<?> expectedType, Optional<DateTime> value, StatementContext ctx) {
        // accepts guarantees that the value is present
        return new JodaDateTimeArgument(value.get(), calendar);
    }
}
