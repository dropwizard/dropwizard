package io.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.time.LocalDate;

/**
 * An {@link ArgumentFactory} for {@link LocalDate} arguments wrapped by Guava's {@link Optional}.
 */
public class GuavaOptionalLocalDateArgumentFactory implements ArgumentFactory<Optional<LocalDate>> {
    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        if (value instanceof Optional) {
            final Optional<?> optionalValue = (Optional<?>) value;
            // Fall through to OptionalArgumentFactory if absent.
            // Fall through to OptionalArgumentFactory if present, but not LocalDate.
            return optionalValue.isPresent() && optionalValue.get() instanceof LocalDate;
        }
        return false;
    }

    @Override
    public Argument build(Class<?> expectedType, Optional<LocalDate> value, StatementContext ctx) {
        // accepts guarantees that the value is present
        return new LocalDateArgument(value.get());
    }
}
