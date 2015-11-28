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
 * An {@link ArgumentFactory} for Joda {@link DateTime} arguments.
 */
public class JodaDateTimeArgumentFactory implements ArgumentFactory<DateTime> {

    /**
     * <p>{@link Calendar} for representing a database time zone.<p>
     * It's needed when an argument is not represented in a database
     * as {@code TIMESTAMP WITH TIME ZONE}. In this case for correct
     * representing of a timestamp an explicit cast to the database
     * time zone is needed at the JDBC driver level.
     */
    private final Optional<Calendar> calendar;

    public JodaDateTimeArgumentFactory() {
        calendar = Optional.empty();
    }

    /**
     * Create an argument factory with a custom time zone offset
     *
     * @param timeZone a time zone representing an offset
     */
    public JodaDateTimeArgumentFactory(Optional<TimeZone> timeZone) {
        calendar = timeZone.map(GregorianCalendar::new);
    }

    @Override
    public boolean accepts(final Class<?> expectedType,
                           final Object value,
                           final StatementContext ctx) {
        return value instanceof DateTime;
    }

    @Override
    public Argument build(final Class<?> expectedType,
                          final DateTime value,
                          final StatementContext ctx) {
        return new JodaDateTimeArgument(value, calendar);
    }
}
