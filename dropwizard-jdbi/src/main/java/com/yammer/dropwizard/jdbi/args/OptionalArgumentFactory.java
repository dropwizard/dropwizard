package com.yammer.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OptionalArgumentFactory implements ArgumentFactory<Optional<Object>> {
    private static class DefaultOptionalArgument implements Argument {
        private final Optional<?> value;

        private DefaultOptionalArgument(Optional<?> value) {
            this.value = value;
        }

        @Override
        public void apply(int position,
                          PreparedStatement statement,
                          StatementContext ctx) throws SQLException {
            if (value.isPresent()) {
                statement.setObject(position, value.get());
            } else {
                statement.setNull(position, Types.OTHER);
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
            statement.setObject(position, value.orNull());
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
        }
        return new DefaultOptionalArgument(value);
    }
}
