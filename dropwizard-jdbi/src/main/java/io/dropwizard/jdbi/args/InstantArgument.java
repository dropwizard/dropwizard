package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.util.Calendar;
import java.util.Optional;

/**
 * An {@link Argument} for {@link Instant} objects.
 */
public class InstantArgument implements Argument {
    private final Instant instant;
    private final Optional<Calendar> calendar;

    protected InstantArgument(final Instant instant, final Optional<Calendar> calendar) {
        this.instant = instant;
        this.calendar = calendar;
    }

    @Override
    public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {
        if (instant != null) {
            if (calendar.isPresent()) {
                // We need to make a clone, because Calendar is not thread-safe
                // and some JDBC drivers mutate it during time calculations
                final Calendar calendarClone = (Calendar) calendar.get().clone();
                statement.setTimestamp(position, Timestamp.from(instant), calendarClone);
            } else {
                statement.setTimestamp(position, Timestamp.from(instant));
            }
        } else {
            statement.setNull(position, Types.TIMESTAMP);
        }
    }
}
