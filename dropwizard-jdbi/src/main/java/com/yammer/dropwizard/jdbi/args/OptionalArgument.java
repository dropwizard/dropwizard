package com.yammer.dropwizard.jdbi.args;

import com.google.common.base.Optional;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class OptionalArgument implements Argument {
    private final Optional<?> value;
    private final String jdbcDriver;

    public OptionalArgument(Optional<?> value, String jdbcDriver) {
        this.value = value;
        this.jdbcDriver = jdbcDriver;
    }

    @Override
    public void apply(int position,
                      PreparedStatement statement,
                      StatementContext ctx) throws SQLException {
        if (jdbcDriver.equals("com.microsoft.sqlserver.jdbc.SQLServerDriver")) {
            statement.setObject(position, value.orNull());
        } else {
            if (value.isPresent()) {
                statement.setObject(position, value.get());
            } else {
                statement.setNull(position, Types.OTHER);
            }
        }
    }
}
