package io.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.time.LocalDateTime;

/**
 * An {@link ArgumentFactory} for {@link LocalDateTime} arguments wrapped by Guava's {@link Optional}.
 */
public class GuavaOptionalLocalDateTimeArgumentFactory implements ArgumentFactory<Optional<LocalDateTime>> {
    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        if (value instanceof Optional) {
            final Optional<?> optionalValue = (Optional<?>) value;
            // Fall through to OptionalArgumentFactory if absent.
            // Fall through to OptionalArgumentFactory if present, but not LocalDateTime.
            return optionalValue.isPresent() && optionalValue.get() instanceof LocalDateTime;
        }
        return false;
    }

    @Override
    public Argument build(Class<?> expectedType, Optional<LocalDateTime> value, StatementContext ctx) {
        // accepts guarantees that the value is present
        return new LocalDateTimeArgument(value.get());
    }
}
