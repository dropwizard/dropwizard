package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

public class OptionalArgumentFactory implements ArgumentFactory<Optional<Object>> {
    private static class DefaultOptionalArgument implements Argument {
        private final Optional<?> value;
        private final int nullType;

        private DefaultOptionalArgument(Optional<?> value, int nullType) {
            this.value = value;
            this.nullType = nullType;
        }

        private DefaultOptionalArgument(Optional<?> value) {
            this(value, Types.OTHER);
        }

        @Override
        public void apply(int position,
                          PreparedStatement statement,
                          StatementContext ctx) throws SQLException {
            if (value.isPresent()) {
                statement.setObject(position, value.get());
            } else {
                statement.setNull(position, nullType);
            }
        }
    }

    private static class MsSqlOptionalArgument implements Argument {
        private final Optional<?> value;

        private MsSqlOptionalArgument(Optional<?> value) {
            this.value = value;
        }

        @Override
        public void apply(int position,
                          PreparedStatement statement,
                          StatementContext ctx) throws SQLException {
            statement.setObject(position, value.orElse(null));
        }
    }

    private final String jdbcDriver;

    public OptionalArgumentFactory(String jdbcDriver) {
        this.jdbcDriver = jdbcDriver;
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Optional;
    }

    @Override
    public Argument build(Class<?> expectedType, Optional<Object> value, StatementContext ctx) {
        if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(jdbcDriver)) {
            return new MsSqlOptionalArgument(value);
        } else if ("oracle.jdbc.OracleDriver".equals(jdbcDriver)) {
            return new DefaultOptionalArgument(value, Types.NULL);
        }
        return new DefaultOptionalArgument(value);
    }
}
