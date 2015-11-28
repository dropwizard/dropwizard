package io.dropwizard.jdbi.args;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.ResultColumnMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * A {@link ResultColumnMapper} to map {@link OffsetDateTime} objects.
 */
public class OffsetDateTimeMapper implements ResultColumnMapper<OffsetDateTime> {

    /**
     * <p>{@link Calendar} for representing a database time zone.<p>
     * If a field is not represented in a database as
     * {@code TIMESTAMP WITH TIME ZONE}, we need to set its time zone
     * explicitly. Otherwise it will not be correctly represented in
     * a time zone different from the time zone of the database.
     */
    private Optional<Calendar> calendar;

    public OffsetDateTimeMapper() {
        calendar = Optional.empty();
    }

    public OffsetDateTimeMapper(Optional<TimeZone> timeZone) {
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
    private Calendar cloneCalendar() {
        return (Calendar) calendar.get().clone();
    }

    @Override
    public OffsetDateTime mapColumn(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        final Timestamp timestamp = calendar.isPresent() ? r.getTimestamp(columnNumber, cloneCalendar()) :
            r.getTimestamp(columnNumber);
        return convertToOffsetDateTime(timestamp);
    }

    @Override
    public OffsetDateTime mapColumn(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        final Timestamp timestamp = calendar.isPresent() ? r.getTimestamp(columnLabel, cloneCalendar()) :
            r.getTimestamp(columnLabel);
        return convertToOffsetDateTime(timestamp);
    }

    private OffsetDateTime convertToOffsetDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        final Optional<ZoneId> zoneId = calendar.flatMap(c -> Optional.of(c.getTimeZone().toZoneId()));
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp.getTime()), zoneId.orElse(ZoneId.systemDefault()));
    }
}
