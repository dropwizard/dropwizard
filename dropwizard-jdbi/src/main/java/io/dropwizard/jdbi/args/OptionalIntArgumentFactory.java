package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.OptionalInt;

public class OptionalIntArgumentFactory implements ArgumentFactory<OptionalInt> {
    private static class DefaultOptionalArgument implements Argument {
        private final OptionalInt value;

        private DefaultOptionalArgument(OptionalInt value) {
            this.value = value;
        }

        @Override
        public void apply(int position,
                          PreparedStatement statement,
                          StatementContext ctx) throws SQLException {
            if (value.isPresent()) {
                statement.setInt(position, value.getAsInt());
            } else {
                statement.setNull(position, Types.INTEGER);
            }
        }
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof OptionalInt;
    }

    @Override
    public Argument build(Class<?> expectedType, OptionalInt value, StatementContext ctx) {
        return new DefaultOptionalArgument(value);
    }
}
