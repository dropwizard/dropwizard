package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

/**
 * An {@link ArgumentFactory} for Joda {@link DateTime} arguments.
 */
public class JodaDateTimeArgumentFactory implements ArgumentFactory<DateTime> {

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
        return new JodaDateTimeArgument(value);
    }
}
