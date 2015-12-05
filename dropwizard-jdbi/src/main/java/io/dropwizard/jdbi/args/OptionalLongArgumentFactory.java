package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.OptionalLong;

public class OptionalLongArgumentFactory implements ArgumentFactory<OptionalLong> {
    private static class DefaultOptionalArgument implements Argument {
        private final OptionalLong value;

        private DefaultOptionalArgument(OptionalLong value) {
            this.value = value;
        }

        @Override
        public void apply(int position,
                          PreparedStatement statement,
                          StatementContext ctx) throws SQLException {
            if (value.isPresent()) {
                statement.setLong(position, value.getAsLong());
            } else {
                statement.setNull(position, Types.BIGINT);
            }
        }
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof OptionalLong;
    }

    @Override
    public Argument build(Class<?> expectedType, OptionalLong value, StatementContext ctx) {
        return new DefaultOptionalArgument(value);
    }
}
