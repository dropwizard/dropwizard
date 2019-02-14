package io.dropwizard.jdbi.args;

import org.joda.time.DateTime;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * A {@link ResultColumnMapper} to map Joda {@link DateTime} objects.
 */
public class JodaDateTimeMapper implements ResultColumnMapper<DateTime> {

    /**
     * <p>{@link Calendar} for representing a database time zone.<p>
     * If a field is not represented in a database as
     * {@code TIMESTAMP WITH TIME ZONE}, we need to set its time zone
     * explicitly. Otherwise it will not be correctly represented in
     * a time zone different from the time zone of the database.
     */
    private Optional<Calendar> calendar;

    public JodaDateTimeMapper() {
        calendar = Optional.empty();
    }

    public JodaDateTimeMapper(Optional<TimeZone> timeZone) {
        calendar = timeZone.map(GregorianCalendar::new);
    }

    /**
     * Make a clone of a calendar.
     * <p>Despite the fact that {@link Calendar} is used only for
     * representing a time zone, some JDBC drivers actually use it
     * for time calculations,</p>
     * <p>Also {@link Calendar} is not immutable, which makes it
     * thread-unsafe. Therefore we need to make a copy to avoid
     * state mutation problems.</p>
     *
     * @return a clone of calendar, representing a database time zone
     */
    private Optional<Calendar> cloneCalendar() {
        return calendar.map(Calendar::clone).map(x -> (Calendar) x);
    }

    @Override
    @Nullable
    public DateTime mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final Optional<Calendar> instance = cloneCalendar();
        final Timestamp timestamp = instance.isPresent() ? r.getTimestamp(columnNumber, instance.get()) :
            r.getTimestamp(columnNumber);
        if (timestamp == null) {
            return null;
        }
        return new DateTime(timestamp.getTime());
    }

    @Override
    @Nullable
    public DateTime mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final Optional<Calendar> instance = cloneCalendar();
        final Timestamp timestamp = instance.isPresent() ? r.getTimestamp(columnLabel, instance.get()) :
            r.getTimestamp(columnLabel);
        if (timestamp == null) {
            return null;
        }
        return new DateTime(timestamp.getTime());
    }
}
