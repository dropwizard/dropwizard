package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.time.LocalDateTime;

/**
 * An {@link ArgumentFactory} for {@link LocalDateTime} arguments.
 */
public class LocalDateTimeArgumentFactory implements ArgumentFactory<LocalDateTime> {

    @Override
    public boolean accepts(final Class<?> expectedType,
                           final Object value,
                           final StatementContext ctx) {
        return value instanceof LocalDateTime;
    }

    @Override
    public Argument build(final Class<?> expectedType,
                          final LocalDateTime value,
                          final StatementContext ctx) {
        return new LocalDateTimeArgument(value);
    }
}
