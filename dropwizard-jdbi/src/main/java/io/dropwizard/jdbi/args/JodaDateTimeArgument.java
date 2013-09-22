package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * An {@link Argument} for Joda {@link DateTime} objects.
 */
public class JodaDateTimeArgument implements Argument {

    private final DateTime value;

    JodaDateTimeArgument(final DateTime value) {
        this.value = value;
    }

    @Override
    public void apply(final int position,
                      final PreparedStatement statement,
                      final StatementContext ctx) throws SQLException {
        if (value != null) {
            statement.setTimestamp(position, new Timestamp(value.getMillis()));
        } else {
            statement.setNull(position, Types.TIMESTAMP);
        }
    }
}
