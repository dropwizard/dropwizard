package com.yammer.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OptionalArgument implements Argument {
    private final Optional<?> value;

    public OptionalArgument(Optional<?> value) {
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
