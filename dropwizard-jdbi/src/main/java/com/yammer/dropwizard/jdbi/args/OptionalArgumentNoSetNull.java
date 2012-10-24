package com.yammer.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OptionalArgumentNoSetNull extends OptionalArgument {
    public OptionalArgumentNoSetNull(Optional<?> value) {
        super(value);
    }

    @Override
    public void apply(int position,
                      PreparedStatement statement,
                      StatementContext ctx) throws SQLException {
        statement.setObject(position, value.orNull());
    }
}