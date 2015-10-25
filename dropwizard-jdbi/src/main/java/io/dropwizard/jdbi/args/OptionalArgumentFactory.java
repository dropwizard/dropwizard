package io.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import static java.util.Objects.requireNonNull;

public class OptionalArgumentFactory implements ArgumentFactory<Optional<Object>> {
    private static final class DefaultAbsentArgument implements Argument {
        @Override
        public void apply(int position,
                          PreparedStatement statement,
                          StatementContext ctx) throws SQLException {
            statement.setNull(position, Types.OTHER);
        }
    }

    private static final class MsSqlAbsentArgument implements Argument {
        @Override
        public void apply(int position,
                          PreparedStatement statement,
                          StatementContext ctx) throws SQLException {
            statement.setObject(position, null);
        }
    }

    private final String jdbcDriver;
    private final ArgumentFactory argumentFactory;

    public OptionalArgumentFactory(String jdbcDriver, ArgumentFactory argumentFactory) {
        this.jdbcDriver = requireNonNull(jdbcDriver);
        this.argumentFactory = requireNonNull(argumentFactory);
    }

    @Override
    public boolean accepts(Class<?> expectedType, Object value, StatementContext ctx) {
        return value instanceof Optional;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Argument build(Class<?> expectedType, Optional<Object> value, StatementContext ctx) {
        if (value.isPresent()) {
            final Object o = value.get();
            return argumentFactory.build(o.getClass(), o, ctx);
        } else {
            if ("com.microsoft.sqlserver.jdbc.SQLServerDriver".equals(jdbcDriver)) {
                return new MsSqlAbsentArgument();
            }

            return new DefaultAbsentArgument();
        }
    }
}
