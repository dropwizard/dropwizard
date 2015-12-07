package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.Optional;

/**
 * An {@link Argument} for Joda {@link DateTime} objects.
 */
public class JodaDateTimeArgument implements Argument {

    private final DateTime value;
    private final Optional<Calendar> calendar;

    JodaDateTimeArgument(final DateTime value, final Optional<Calendar> calendar) {
        this.value = value;
        this.calendar = calendar;
    }

    @Override
    public void apply(final int position,
                      final PreparedStatement statement,
                      final StatementContext ctx) throws SQLException {
        if (value != null) {
            if (calendar.isPresent()) {
                // We need to make a clone, because Calendar is not thread-safe
                // and some JDBC drivers mutate it during time calculations
                final Calendar calendarClone = (Calendar) calendar.get().clone();
                statement.setTimestamp(position, new Timestamp(value.getMillis()), calendarClone);
            } else {
                statement.setTimestamp(position, new Timestamp(value.getMillis()));
            }
        } else {
            statement.setNull(position, Types.TIMESTAMP);
        }
    }
}
