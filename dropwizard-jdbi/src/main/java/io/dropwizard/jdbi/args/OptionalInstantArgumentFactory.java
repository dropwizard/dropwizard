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
 * An {@link ArgumentFactory} for {@link Instant} arguments wrapped by {@link Optional}.
 */
public class OptionalInstantArgumentFactory implements ArgumentFactory<Optional<Instant>> {

    private final Optional<Calendar> calendar;

    public OptionalInstantArgumentFactory() {
        calendar = Optional.empty();
    }

    public OptionalInstantArgumentFactory(Optional<TimeZone> timeZone) {
        calendar = timeZone.map(GregorianCalendar::new);
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        if (value instanceof Optional) {
            final Optional<?> optionalValue = (Optional<?>) value;
            // Fall through to OptionalArgumentFactory if absent.
            // Fall through to OptionalArgumentFactory if present, but not Instant.
            return optionalValue.isPresent() && optionalValue.get() instanceof Instant;
        }
        return false;
    }

    @Override
    public Argument build(Class<?> expectedType, Optional<Instant> value, StatementContext ctx) {
        // accepts guarantees that the value is present
        return new InstantArgument(value.get(), calendar);
    }
}
