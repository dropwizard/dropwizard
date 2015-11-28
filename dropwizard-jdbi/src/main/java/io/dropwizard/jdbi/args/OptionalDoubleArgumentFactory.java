package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.OptionalDouble;

public class OptionalDoubleArgumentFactory implements ArgumentFactory<OptionalDouble> {
    private static class DefaultOptionalArgument implements Argument {
        private final OptionalDouble value;

        private DefaultOptionalArgument(OptionalDouble value) {
            this.value = value;
        }

        @Override
        public void apply(int position,
                          PreparedStatement statement,
                          StatementContext ctx) throws SQLException {
            if (value.isPresent()) {
                statement.setDouble(position, value.getAsDouble());
            } else {
                statement.setNull(position, Types.DOUBLE);
            }
        }
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof OptionalDouble;
    }

    @Override
    public Argument build(Class<?> expectedType, OptionalDouble value, StatementContext ctx) {
        return new DefaultOptionalArgument(value);
    }
}
