package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;

/**
 * An {@link Argument} for {@link LocalDate} objects.
 */
public class LocalDateArgument implements Argument {

    private final LocalDate value;

    public LocalDateArgument(LocalDate value) {
        this.value = value;
    }

    @Override
    public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
        if (value != null) {
            statement.setTimestamp(position, Timestamp.valueOf(value.atStartOfDay()));
        } else {
            statement.setNull(position, Types.TIMESTAMP);
        }
    }
}
