package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.time.Period;

/**
 * An {@link ArgumentFactory} for {@link Period} arguments.
 */
public class PeriodArgumentFactory implements ArgumentFactory<Period> {

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Period;
    }

    @Override
    public Argument build(Class<?> expectedType, Period value, StatementContext ctx) {
        return new PeriodArgument(value);
    }
}
